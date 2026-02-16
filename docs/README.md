# Documentation Site

This directory contains the GitHub Pages documentation for NetworkResponse Adapter.

## Local Development

To run the documentation site locally:

1. Install Jekyll:
   ```bash
   gem install bundler jekyll
   ```

2. Navigate to the docs directory:
   ```bash
   cd docs
   ```

3. Install dependencies:
   ```bash
   bundle install
   ```

4. Run the local server:
   ```bash
   bundle exec jekyll serve
   ```

5. Open http://localhost:4000 in your browser

## Deployment

The site is automatically deployed to GitHub Pages when changes are pushed to the main branch.

## Structure

- `index.md` - Home page
- `installation.md` - Installation guide
- `quickstart.md` - Quick start guide
- `advanced.md` - Advanced features
- `api.md` - API reference
- `_config.yml` - Jekyll configuration
