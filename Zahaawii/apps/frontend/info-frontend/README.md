# info-frontend

Standalone static CV/info microsite intended for `info.zaak.dk`.

## Structure

- `index.html` is the public read-only page and admin login shell.
- `styles.css` contains the microsite-specific responsive styling.
- `data.js` is the data source for profile, projects, experience, and contact content.
- `app.js` renders the page and enables authenticated local editing.

## Admin Editing

The page loads the existing frontend auth helper from `../auth.js` and uses `window.authClient` when available. Edit mode is intentionally static: saved edits are stored as a browser-local draft in `localStorage`.

To publish content for all visitors, update `data.js` with the desired content and deploy the static folder.
