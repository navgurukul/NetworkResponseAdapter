# Publishing Guide

This library is published to both JitPack and GitHub Packages.

## How to Publish to JitPack

1. **Push your code to GitHub**
   ```bash
   git add .
   git commit -m "Release version 1.0.0"
   git push origin main
   ```

2. **Create a GitHub Release**
   - Go to your GitHub repository
   - Click on "Releases" → "Create a new release"
   - Tag version: `1.0.0`
   - Release title: `v1.0.0`
   - Click "Publish release"

3. **Build on JitPack**
   - Go to https://jitpack.io
   - Enter your repository URL: `https://github.com/navgurukul/NetworkResponseAdapter`
   - Click "Look up"
   - Click "Get it" next to version 1.0.0
   - JitPack will build your library

4. **Use in Projects**
   
   Add to your root build.gradle:
   ```gradle
   allprojects {
       repositories {
           maven { url 'https://jitpack.io' }
       }
   }
   ```
   
   Add dependency:
   ```gradle
   dependencies {
       implementation 'com.github.navgurukul:NetworkResponseAdapter:1.0.0'
   }
   ```

## Local Testing

Test the library locally before publishing:

```bash
./gradlew :networkresponse:publishToMavenLocal
```

Then in another project, add to build.gradle:
```gradle
repositories {
    mavenLocal()
}

dependencies {
    implementation 'com.github.navgurukul:network-response-adapter:1.0.0'
}
```

## Version Updates

To release a new version:
1. Update version in `networkresponse/build.gradle.kts`
2. Create a new GitHub release with the new version tag
3. JitPack will automatically build the new version

---

## How to Publish to GitHub Packages

GitHub Packages publishing is automated via GitHub Actions.

### Automatic Publishing (Recommended)

When you create a GitHub Release, the library automatically publishes to GitHub Packages:

1. **Update version** in `networkresponse/build.gradle.kts`
2. **Create a GitHub Release**
   - Go to your GitHub repository
   - Click "Releases" → "Create a new release"
   - Tag version: `1.0.1` (or your version)
   - Release title: `v1.0.1`
   - Click "Publish release"
3. **GitHub Actions will automatically**:
   - Build the library
   - Publish to GitHub Packages
   - Upload build artifacts

### Manual Publishing

To publish manually from your local machine:

1. **Create a Personal Access Token**
   - Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
   - Click "Generate new token (classic)"
   - Select scopes: `write:packages`, `read:packages`
   - Copy the token

2. **Add credentials to `~/.gradle/gradle.properties`**
   ```properties
   gpr.user=YOUR_GITHUB_USERNAME
   gpr.token=YOUR_PERSONAL_ACCESS_TOKEN
   ```

3. **Publish**
   ```bash
   ./gradlew :networkresponse:publish
   ```

### Using the Library from GitHub Packages

Add to your project's `build.gradle.kts`:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/navgurukul/NetworkResponseAdapter")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.github.navgurukul:network-response-adapter:1.0.0")
}
```

**Note**: GitHub Packages requires authentication even for public packages. Users need to:
1. Create a GitHub Personal Access Token with `read:packages` scope
2. Add credentials to their `~/.gradle/gradle.properties`

### Why Use Both JitPack and GitHub Packages?

- **JitPack**: No authentication required, easier for public consumption
- **GitHub Packages**: Official GitHub integration, better for private repos and enterprise use

Most users should use JitPack for simplicity. GitHub Packages is available for those who prefer it or need enterprise features.
