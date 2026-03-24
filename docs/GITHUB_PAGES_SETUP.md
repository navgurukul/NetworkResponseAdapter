# GitHub Pages Setup Guide

This guide will help you publish your documentation to GitHub Pages.

## Step 1: Enable GitHub Pages

1. Go to your repository on GitHub: `https://github.com/navgurukul/NetworkResponseAdapter`
2. Click on **Settings** (top right)
3. Scroll down to **Pages** in the left sidebar
4. Under **Source**, select:
   - Branch: `main` (or your default branch)
   - Folder: `/docs`
5. Click **Save**

## Step 2: Wait for Deployment

GitHub will automatically build and deploy your site. This usually takes 1-2 minutes.

You can check the deployment status:
- Go to the **Actions** tab in your repository
- Look for "pages build and deployment" workflow

## Step 3: Access Your Site

Once deployed, your documentation will be available at:

```
https://navgurukul.github.io/NetworkResponseAdapter/
```

## Step 4: Update GitHub About Section

Now you can add this URL to your repository's About section:

1. Go to your repository homepage
2. Click the ⚙️ (gear icon) next to "About"
3. Add the website URL: `https://navgurukul.github.io/NetworkResponseAdapter/`
4. Add description: "Type-safe Retrofit CallAdapter for elegant network response handling with built-in caching, retry mechanism, and coroutines support for Android"
5. Add topics: `android`, `kotlin`, `retrofit`, `networking`, `call-adapter`, `error-handling`, `coroutines`, `caching`, `type-safe`, `retrofit2`, `okhttp`, `android-library`
6. Click **Save changes**

## Custom Domain (Optional)

If you want to use a custom domain like `networkresponse.navgurukul.org`:

1. Add a `CNAME` file in the `docs` folder with your domain name
2. Configure DNS settings with your domain provider:
   - Add a CNAME record pointing to `navgurukul.github.io`
3. In GitHub Pages settings, add your custom domain
4. Enable "Enforce HTTPS"

## Local Testing

To test the site locally before pushing:

```bash
cd docs
gem install bundler jekyll
bundle install
bundle exec jekyll serve
```

Then open http://localhost:4000

## Updating Documentation

Simply edit the markdown files in the `docs` folder and push to GitHub. The site will automatically rebuild.

## Files Created

- `docs/index.md` - Home page
- `docs/installation.md` - Installation guide
- `docs/quickstart.md` - Quick start guide
- `docs/advanced.md` - Advanced features
- `docs/api.md` - API reference
- `docs/_config.yml` - Jekyll configuration
- `docs/Gemfile` - Ruby dependencies

## Troubleshooting

If the site doesn't appear:
1. Check the Actions tab for build errors
2. Ensure GitHub Pages is enabled in Settings
3. Verify the `/docs` folder is selected as the source
4. Wait a few minutes for DNS propagation

## Next Steps

- Customize the theme in `_config.yml`
- Add more pages as needed
- Update content to match your latest features
- Consider adding a blog section for release notes
