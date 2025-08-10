# Commit Rules

## Format

```
<type>(<scope>): <subject>

<body - optional>

<footer - optional>
```

## Rules

### 1. **Type** (Required)

Must be one of the following:

- `feat` - A new feature for the user
- `fix` - A bug fix
- `docs` - Documentation changes
- `style` - Code style changes (formatting, missing semicolons, etc.)
- `refactor` - Code refactoring without changing functionality
- `perf` - Performance improvements
- `test` - Adding or updating tests
- `chore` - Maintenance tasks, dependency updates
- `ci` - CI/CD pipeline changes
- `build` - Build system or external dependencies changes
- `revert` - Reverting a previous commit

### 2. **Scope** (Optional)

- Use lowercase
- Represents the module, component, or area affected
- Examples: `auth`, `api`, `ui`, `database`, `payment`
- Can be omitted if change affects multiple areas

### 3. **Subject** (Required)

- Use imperative mood ("add" not "added" or "adding")
- Start with lowercase letter
- No period at the end
- Maximum 50 characters
- Describe what the commit does, not what was done

### 4. **Body** (Optional)

- Use when commit needs explanation beyond the subject
- Wrap at 72 characters per line
- Explain the "what" and "why", not the "how"
- Separate from subject with blank line

### 5. **Footer** (Optional)

- Reference issues/tickets: `Closes #123`, `Fixes #456`
- Breaking changes: `BREAKING CHANGE: description`
- Co-authored commits: `Co-authored-by: Name <email>`

## Examples

### Simple commits:

```
feat(auth): add OAuth2 login support
fix(api): handle null user data in profile endpoint
docs: update installation guide
```

### Complex commits:

```
feat(payment): integrate Stripe payment processing

Add support for credit card payments using Stripe API.
Includes payment form validation and error handling.

Closes #234
```

### Breaking changes:

```
feat(api): change user endpoint response format

BREAKING CHANGE: User API now returns 'userId' instead of 'id'
```

## Best Practices

1. **Keep commits atomic** - One logical change per commit
2. **Write for your future self** - Clear, descriptive messages
3. **Test before committing** - Ensure code works
4. **Use consistent tense** - Always imperative mood
5. **Reference issues** - Link to tickets when applicable
6. **Separate concerns** - Don't mix feature and formatting changes
