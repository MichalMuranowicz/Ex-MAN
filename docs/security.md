# Security

This repository is intended for portfolio presentation and should not contain private operational data, credentials or release secrets.

## Security Assumptions

- The application requires the user to authenticate in the external web workflow.
- Credentials are not stored in the repository.
- Exported business data is treated as private operational output.
- Build artifacts should be distributed separately from source code.

## Forbidden Repository Content

Do not commit:

- credentials,
- cookies,
- tokens,
- real exported operational data,
- private keys,
- keystore files,
- APK build artifacts unless published through GitHub Releases,
- EXE build artifacts unless published through GitHub Releases,
- local configuration files containing private paths or secrets.

## Desktop Credential Handling

The Windows version may support local credential saving when the user explicitly chooses it. If saved, credentials should rely on secure local mechanisms tied to the current Windows user profile.

Saved local credentials must never be committed to the repository.

## Android Credential Handling

The Android version should not store portal credentials locally. Authentication should happen inside the WebView workflow.

## Git Ignore Protection

The `.gitignore` file should protect common sensitive and generated files, including:

- Python cache files,
- virtual environments,
- logs,
- exported `.xlsx` files,
- local config files,
- `.env` files,
- build folders,
- dist folders,
- EXE files,
- APK and AAB files,
- keystore files,
- private keys,
- Android `local.properties`,
- Gradle build folders.

This is a practical repository hygiene note, not a formal security audit.

