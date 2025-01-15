var config = require('semantic-release-preconfigured-conventional-commits');

config.plugins.push(
  "@semantic-release/github",
  "@semantic-release/git",
  [
    "@semantic-release/exec",
    {
      successCmd: 'echo "Release succeeded!"',
      failCmd: 'echo "Release failed!"',
    },
  ]
);

module.exports = config;

