# IP Range Filter API

A  Spring Boot-based REST API to fetch and filter GCP IP ranges by region and IP version ("IPv4", "IPv6", or both).

## Features

- Fetches GCP IP prefixes from https://www.gstatic.com/ipranges/cloud.json
- Filters results by geographic region ("EU": Europe, "AS": Asia, "US": America, etc.)
- Supports "IPv4", "IPv6", or both
- Built using Spring Boot & Kotlin
- Dockerized for easy deployment

## Tech Stack

- Kotlin + Spring Boot  
- Maven  
- WebClient (non-blocking HTTP calls)  
- Docker  
- GitHub Actions CI/CD

##  Run Locally

### 1. Clone the repo
```bash
git clone https://github.com/<your-username>/iprange-filter.git
cd iprange-filter
```
### 2. Build and Run the Project
```bash
mvn clean package
java -jar target/iprange-filter.jar
```
### 3. Access the API
GET http://localhost:8080/ip-ranges?region=EU&ipType=ipv4

### Docker Usage
```bash
docker build -t ninad0901/iprange-filter-repo:latest .
docker run -p <host-port>:8080 ninad0901/iprange-filter-repo:latest
```
