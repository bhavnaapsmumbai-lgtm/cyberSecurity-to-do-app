# Security Testing

This document lists the threats I considered for this app, the control that addresses each one, how I tested it, and what happened.

## Test log

| # | Threat | Control | How I tested it | Result |
|---|--------|---------|-----------------|--------|
| 1 | A logged-in user reads another user's tasks | Firestore security rules: access to `users/{userId}/tasks` is allowed only when the signed-in user's ID matches `userId` | Created tasks in Account A, logged out, created Account B on the same emulator, and checked B's task list | Account B saw an empty list. Account A's tasks were not visible. **Pass** |
| 2 | Oversized or malformed task data written to the database | Firestore rules: a task may contain only `text` and `createdAt`, and `text` must be a string of 1 to 200 characters. The app's input box also stops at 200 characters | Published the rules, added normal tasks in the app, and typed until the input box stopped at 200 characters | Normal tasks save and the box stops at 200. The rule's rejection of bad data has not been tested directly yet (Rules Playground test planned). **Partial** |
| 3 | Account enumeration: an attacker uses the password reset to find out which emails are registered | The reset screen shows the same message whether or not the email has an account | Requested a reset for a registered email and for an email with no account, and compared the messages | Both showed the same message. An email arrived only for the registered address. **Pass** |
| 4 | Registering with an email the person doesn't own | Email verification: the app only lets verified accounts past login and onto the to-do screen | Created an account with a real email, tried to log in before verifying, then clicked the link and logged in again | Login was refused before verifying and allowed after. **Pass** |
| 5 | An unverified account talks to the database directly, skipping the app | Firestore rules require `request.auth.token.email_verified == true` for every read and write | Published the rules and confirmed a verified account still reads and writes tasks. Direct refusal of an unverified account not yet tested (Rules Playground test planned) | **Partial** |

## Controls in place

- Firebase Authentication handles password storage (passwords are never stored in plain text by the app).
- Input validation on login: email format and minimum password length.
- `MainActivity` is not exported, so other apps cannot open it directly.
- `MainActivity` checks for a logged-in user with a verified email and returns to the login screen if there isn't one.
- Login errors are generic and don't reveal whether an email is registered.
- Firestore rules validate task fields and length (1 to 200 characters).
- Password reset with a generic response, so registered emails can't be discovered.
- Email verification, enforced in the app and in the Firestore rules.

## Known limitations

- No password strength rules beyond a 6-character minimum.
- No way to delete tasks yet.
- Tested only on an emulator, with a few accounts.

## Planned tests

- Use the Rules Playground in the Firebase console to try reading or writing another user's path directly.
- Use the Rules Playground to try writing a task longer than 200 characters, or with an extra field, to confirm the server rejects it.
- Use the Rules Playground to try a request from an unverified account, to confirm the server rejects it.
