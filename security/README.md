# security testing

## auth_scan.py

sends malformed jwts to the running api and checks each one gets the right response.

what it sends:
- no auth header (want 401)
- a token that isn't a jwt (want 401)
- a real token with the signature tampered (want 401)
- an unsigned alg none token (want 401)
- a token signed with the wrong key (want 401)
- an expired token (want 401)

it sends a valid token first that has to come back 200, so a green run means the
forgeries were rejected and normal login still works.

run:
```
python auth_scan.py
python auth_scan.py --base-url http://<public-ip>:8080
```

standard library only, nothing to install. the expired token check needs the server's own
key, so it reads JWT_SECRET from the environment and skips that one check if it isn't set.

exit code is 0 only if everything passes, 1 if a check failed, 2 if setup failed or
the server wasn't reachable. only run this against my own app.

## why bother when junit already covers this

they cover different layers.

junit logs in the normal way, so it always has a valid token in hand. that works for the
ownership rule (can user B reach user A's data) but it never sends a malformed token,
because a normal login can't produce one.

this script builds the tokens by hand out of the raw base64 sections, so it can send
what no real client would: a signature that doesn't match, a token with no signature at
all, one signed with someone else's key, one that has already expired. those are the
standard ways of trying to get past jwt auth, and the test suite doesn't cover any of them.

it also runs against the real server over http so the full filter chain is involved,
and being a standalone script it can be run in a demo or wired into ci later.
