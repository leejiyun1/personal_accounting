# Commit Log Playbook

This guide defines a consistent commit and logging workflow for the Personal Accounting project (backend and frontend).

## 1) Commit Numbering Rule

- Use a 3-digit sequence prefix in every commit subject.
- Format: `NNN <type>(<scope>): <summary>`
- Example: `001 feat(frontend): unify protected routing with AppLayout`
- Number allocation:
  - One shared sequence across backend/frontend work batches.
  - Increment only after a real commit is created.
  - Do not reuse a number.

## 2) Required Commit Message Structure

- Subject:
  - `NNN <type>(<scope>): <summary>`
- Body (recommended bullets):
  - What changed
  - Why it changed
  - Impact/risk
  - Verification commands run

## 3) Notion Commit Log Required Fields

- `Name`: `NNN - <short work title>`
- `Author`: `ijiyun, Codex` (or actual pair)
- `Branch`: branch name used for commit
- `Repository`: `personal-accounting` or `personal-accounting-frontend`
- `Commit Hash`: short hash (e.g. `16ce7c0`)
- `Summary`: concise description with key impact
- `Commit Date`: commit date

## 4) Work Batch Procedure

1. Confirm scope with the user before implementation.
2. Implement only the approved batch.
3. Run verification commands.
4. Commit with numbered message.
5. Update corresponding Notion Todo item status and result.
6. Create one Commit Log entry in Notion.

## 5) Verification Checklist (Before Commit)

- Frontend batch:
  - `npm run lint`
  - `npm run build`
- Backend batch:
  - `./gradlew test`
- For mixed changes, run both suites relevant to touched files.

## 6) Post-Work Review Checklist (After Commit)

- Did API/error response contract change?
  - If yes, check frontend parsing paths and user-facing messages.
- Did auth/session behavior change?
  - If yes, verify login, refresh, logout, protected route access.
- Did data-fetch strategy change?
  - If yes, check loading, retry, and partial-failure handling.
- Did large component refactoring happen?
  - If yes, verify same UI behavior and interactions.

## 7) Rollback Note Template

When risk is non-trivial, add rollback guidance to the Notion summary:
- Revert commit hash
- Files/components most affected
- Expected side effects of rollback
