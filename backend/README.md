# Deploying

Since we're using a mono repo we need to deploy just a sub-directory

`git subtree push --prefix server heroku master`

Automatic heroku deploys won't work. Need to look into that.

