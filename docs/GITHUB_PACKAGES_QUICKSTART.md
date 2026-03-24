# GitHub Packages Quick Start

## ✅ What's Set Up

Your library is now configured to publish to both:
- **JitPack** (existing, no auth required)
- **GitHub Packages** (new, requires auth)

## 🚀 How to Publish

### Option 1: Automatic (Recommended)
Just create a GitHub Release - that's it! The workflow will automatically publish to GitHub Packages.

### Option 2: Manual
```bash
# Set environment variables
export GITHUB_ACTOR=your-username
export GITHUB_TOKEN=your-personal-access-token

# Publish
./gradlew :networkresponse:publish
```

## 📦 How Users Install

### From JitPack (Easier - No Auth)
```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.navgurukul:NetworkResponseAdapter:1.0.0")
}
```

### From GitHub Packages (Requires Auth)
```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/navgurukul/NetworkResponseAdapter")
        credentials {
            username = project.findProperty("gpr.user") as String?
            password = project.findProperty("gpr.token") as String?
        }
    }
}

dependencies {
    implementation("com.github.navgurukul:network-response-adapter:1.0.0")
}
```

Users need to add to `~/.gradle/gradle.properties`:
```properties
gpr.user=their-github-username
gpr.token=their-personal-access-token
```

## 🔑 Creating a Personal Access Token

1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Select scopes: `write:packages` (for publishing), `read:packages` (for consuming)
4. Copy the token

## 💡 Recommendation

For public libraries, recommend users to use JitPack (simpler, no auth). GitHub Packages is great for:
- Private repositories
- Enterprise environments
- Organizations that prefer GitHub-native solutions

## 🔍 Verify Publication

After publishing, check:
- https://github.com/navgurukul/NetworkResponseAdapter/packages
- You should see your package listed there
