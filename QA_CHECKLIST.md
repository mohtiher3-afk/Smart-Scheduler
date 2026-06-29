# QA Checklist: Smart Scheduler

## Pre-Release/Pre-Migration Checklist
- [ ] **Build**: Project builds successfully via `gradle assembleDebug`.
- [ ] **Lint**: No critical warnings/errors.
- [ ] **Deprecated**: All deprecated APIs are addressed.
- [ ] **Theme**: Looks correct in Light and Dark modes.
- [ ] **Accessibility**:
  - [ ] TalkBack works on all interactive elements.
  - [ ] Contrast ratio meets WCAG standards.
  - [ ] Dynamic font scaling does not break UI.
  - [ ] Touch targets are at least 48dp x 48dp.
- [ ] **RTL**: Layout supports RTL correctly.
- [ ] **Performance**: No jank detected during navigation/interaction.
- [ ] **Security**: No secrets hardcoded.
- [ ] **Logic**: Core business logic remains functional (checked via manual testing/existing tests).
