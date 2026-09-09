FROM maven:3.9-eclipse-temurin-17

# Install Google Chrome (headless target used by the containerized run).
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    --no-install-recommends \
 && mkdir -p /etc/apt/keyrings \
 && wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /etc/apt/keyrings/google-chrome.gpg \
 && echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/google-chrome.gpg] http://dl.google.com/linux/chrome/deb/ stable main" \
    > /etc/apt/sources.list.d/google-chrome.list \
 && apt-get update && apt-get install -y google-chrome-stable --no-install-recommends \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

# Pre-download dependencies so the test run itself is fast.
RUN mvn dependency:go-offline -q

# CI=true triggers headless mode in BaseTest.
ENV CI=true
ENV BROWSER=chrome

CMD ["sh", "-c", "mvn test -Dselenide.browser=${BROWSER}"]
