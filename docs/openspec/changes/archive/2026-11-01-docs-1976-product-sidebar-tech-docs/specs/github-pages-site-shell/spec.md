## ADDED Requirements

### Requirement: The Product section has its own pages and sidebar

The Product section SHALL be a set of pages under `/product/` rendered by the shared document shell, with a sidebar
of the section's pages, breadcrumbs and previous/next links, like the Technical docs and the AI Factory. The header's
Product entry SHALL open `/product/`.

#### Scenario: Reader browses the product pages

- **WHEN** a reader opens `/product/story/`
- **THEN** the sidebar lists Overview, Story, Platform and Numbers, the breadcrumbs read Home › Product › Story, and
  previous/next link Overview and Platform

#### Scenario: Visitor lands on the home page

- **WHEN** a visitor opens `/`
- **THEN** the page links the Product, Technical docs and AI Factory sections
