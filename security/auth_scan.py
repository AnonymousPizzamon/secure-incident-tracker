#!/usr/bin/env python3
# sends malformed jwts to the running api and checks each one gets the right response,
# the junit tests always log in normally so they only ever hold a valid token and none
# of these cases show up there. only run this against an app you own.
#
# what it sends:
#   no auth header
#   a token that is not a jwt
#   real token, signature tampered with
#   alg none, no signature
#   signed with a key the server does not have
#   correctly signed but expired
#
# start the app first then:
#   python auth_scan.py
#   python auth_scan.py --base-url http://<public-ip>:8080

import argparse
import base64
import hashlib
import hmac
import json
import os
import sys
import time
import urllib.error
import urllib.request
import uuid

# any key the server does not have, the value does not matter
ATTACKER_SECRET = "attacker-secret-0123456789-0123456789"

# one True or False per check, totalled at the end
results = []


def request(base_url, method, path, body=None, token=None):
    # never raises on a non 2xx, reading the rejection is the point
    url = base_url.rstrip("/") + path
    data = json.dumps(body).encode("utf-8") if body is not None else None

    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token is not None:
        req.add_header("Authorization", "Bearer " + token)

    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            return resp.status, resp.read().decode("utf-8", errors="replace")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", errors="replace")
    except urllib.error.URLError as e:
        print("could not reach " + url + ": " + str(e.reason))
        sys.exit(2)


def get_me(base_url, token=None):
    # every check hits the same protected endpoint, only the token changes
    status, _ = request(base_url, "GET", "/users/me", token=token)
    return status


# a jwt is three base64url sections joined by dots, header.payload.signature

def b64url(raw):
    # base64url, padding stripped, the way jwt encodes
    return base64.urlsafe_b64encode(raw).rstrip(b"=").decode("ascii")


def b64url_json(obj):
    # dict to compact json then base64url, for the header and payload
    return b64url(json.dumps(obj, separators=(",", ":")).encode("utf-8"))


def hmac_sign(header, payload, secret):
    # hs256 over header.payload, same thing a jwt library does.
    # server only accepts a signature made with its own secret
    signing_input = (header + "." + payload).encode("utf-8")
    return b64url(hmac.new(secret.encode("utf-8"), signing_input, hashlib.sha256).digest())


# each one produces a token the server should reject

def break_signature(token):
    # real header and payload, one character of the signature changed so it stops matching
    header, payload, signature = token.split(".")
    swapped = ("C" if signature[0] == "B" else "B") + signature[1:]
    return header + "." + payload + "." + swapped


def forge_alg_none(token):
    # header claims no signature needed, signature left empty.
    # payload is the real one so it still claims to be the victim
    _, payload, _ = token.split(".")
    return b64url_json({"alg": "none", "typ": "JWT"}) + "." + payload + "."


def forge_wrong_key(token, secret):
    # correctly formed hs256 signature, made with a secret the server does not have
    header, payload, _ = token.split(".")
    return header + "." + payload + "." + hmac_sign(header, payload, secret)


def forge_expired(secret, username):
    # signed with the real key so the signature verifies and only the expiry is wrong,
    # otherwise it fails on the signature and never reaches the expiry check
    header = b64url_json({"alg": "HS256", "typ": "JWT"})
    now = int(time.time())
    payload = b64url_json({"sub": username, "iat": now - 7200, "exp": now - 3600})
    return header + "." + payload + "." + hmac_sign(header, payload, secret)


def check(name, status, want=401):
    # one result line, records whether it passed
    passed = status == want
    print("  [" + ("PASS" if passed else "FAIL") + "] " + name)
    print("         -> " + str(status) + " (want " + str(want) + ")")
    results.append(passed)


def skip(name, why):
    # not counted either way
    print("  [SKIP] " + name)
    print("         " + why)


def stop(message):
    # setup failure not a check failure, different exit code
    print("setup failed: " + message)
    sys.exit(2)


def register_and_login(base_url, prefix):
    # new user and login, random suffix so repeat runs do not collide on a username
    username = prefix + "_" + uuid.uuid4().hex[:8]
    body = {"username": username, "password": "password123", "email": username + "@test.local"}

    status, text = request(base_url, "POST", "/auth/register", body=body)
    if status not in (200, 201):
        stop("register failed: " + str(status) + " " + text[:200])

    status, text = request(base_url, "POST", "/auth/login",
                           body={"username": username, "password": "password123"})
    if status != 200:
        stop("login failed: " + str(status) + " " + text[:200])

    token = json.loads(text).get("token")   # login response returns the jwt in "token"
    if not token:
        stop("login returned no token: " + text[:200])
    return username, token


def main():
    parser = argparse.ArgumentParser(description="malformed jwt checks against a running instance")
    parser.add_argument("--base-url", default="http://localhost:8080")
    base_url = parser.parse_args().base_url

    print("target: " + base_url)
    print()

    victim, token = register_and_login(base_url, "victim")

    # real token has to be accepted first, otherwise every rejection below proves nothing
    # since a server that refused everything would pass too
    status = get_me(base_url, token)
    check("control: real token is accepted", status, want=200)
    if status != 200:
        print()
        print("control failed, stopping")
        sys.exit(2)

    print()

    check("no auth header is rejected", get_me(base_url))
    check("garbage token is rejected", get_me(base_url, "not-a-jwt-at-all"))
    check("tampered signature is rejected", get_me(base_url, break_signature(token)))
    check("alg none forgery is rejected", get_me(base_url, forge_alg_none(token)))
    check("wrong-key signature is rejected", get_me(base_url, forge_wrong_key(token, ATTACKER_SECRET)))

    # needs the server's own key, without it the token fails on the signature and the
    # expiry check is never reached
    secret = os.environ.get("JWT_SECRET")
    if secret:
        check("expired token is rejected", get_me(base_url, forge_expired(secret, victim)))
    else:
        skip("expired token is rejected", "set JWT_SECRET to test this")

    passed = results.count(True)
    print()
    print(str(passed) + "/" + str(len(results)) + " passed")
    sys.exit(0 if passed == len(results) else 1)


if __name__ == "__main__":
    main()
