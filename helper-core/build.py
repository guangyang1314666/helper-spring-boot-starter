import os

os.system("mvn clean deploy -P release -Dmaven.test.skip=true")

input("Press Any Key Close")