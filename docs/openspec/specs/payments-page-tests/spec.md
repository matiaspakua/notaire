# payments-page-tests Specification

## Purpose
Frontend unit rules for the payments page (CU47): form validation, balance and
receipt paths, budget consultation, and inclusive date-range address updates.
Source: #2114 / #2115; date-query mock honesty #2119.

## Requirements

### Requirement: The payments page rules are unit tested

The frontend unit suite MUST assert that a payment needs a budget, a date and an amount, that an edit is saved with PUT without the budget, that a conflict without a reason explains the exceeded balance, and that the list narrows by budget and by an inclusive date range. When the suite drives date bounds through the page's address helpers, the navigation mock MUST apply each `router.replace` to the next `useSearchParams` value so assertions mirror `updateQuery` copying existing params.

#### Scenario: an exceeded balance is explained

- **WHEN** a save is rejected with 409 and no reason
- **THEN** the exceeded-balance message is shown

#### Scenario: a range that ends before it starts lists nothing

- **WHEN** the start day is after the end day
- **THEN** the range error is shown and no payment is listed

#### Scenario: typing both days keeps both keys in the address

- **WHEN** the operator types `from=2026-02-01` and then `to=2026-04-30`
- **THEN** `router.replace` is called with `/dashboard/payments?from=2026-02-01&to=2026-04-30`

#### Scenario: clearing the start day leaves the end day

- **WHEN** both days are in the address and the operator clears `from`
- **THEN** `router.replace` is called with `/dashboard/payments?to=2026-04-30`
