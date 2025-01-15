var config = require('semantic-release-preconfigured-conventional-commits');

config.plugins.push(
    "@semantic-release/git",
    "@semantic-release/github",
    [
        "@semantic-release/exec",
        {
            // Pre-configured for version management
            prepareCmd: 'echo "Preparing version ${nextRelease.version}"',
            publishCmd: 'echo "Publishing version ${nextRelease.version}"',
            // Status tracking for GitHub Actions
            successCmd: 'echo "::set-output name=release_created::true"',
            failCmd: 'echo "::set-output name=release_created::false"'
        }
    ]
);

// Already includes:
// - @semantic-release/commit-analyzer
// - @semantic-release/release-notes-generator
// - @semantic-release/changelog

module.exports = config;

