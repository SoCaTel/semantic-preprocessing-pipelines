#####################################################################################
# 																					#
# This is an unofficial dockerfile to run LinkedPipes (https://linkedpipes.com/)	#
# as a Docker container. It was created as part of the SoCaTel H2020 project 	 	#
# (https://www.socatel.eu/) by Rizkallah Touma.										#
# 																					#
# Contact: Rizkallah Touma 															#
# Company: Everis 																	#
# Email: rizk.allah.touma@everis.com 												#
# 																					#
#####################################################################################

# Use a maven base image to build LinkedPipes
FROM maven:3-jdk-8-slim

# Install necessary libraries
RUN apt-get update; apt-get upgrade -y
RUN apt-get install -y curl software-properties-common gnupg; curl -sL https://deb.nodesource.com/setup_10.x | bash -
RUN apt-get install -y nodejs
RUN node -v; npm -v

# Set the working directory to /lp
WORKDIR /lp

# Copy contents to docker
COPY . .

# Build LinkedPipes
RUN cd etl && mvn install -DskipTests

# Build custom LinkedPipes components
RUN cd custom-dependencies && mvn install -DskipTests

# Use a JRE alpine image as a parent image
FROM openjdk:8-jre-alpine

# Install basic tools
RUN apk add --no-cache bash curl gnupg

# Install NodeJS libraries
RUN apk add --no-cache --update nodejs npm
RUN node -v; npm -v

# Set the working directory to /lp
WORKDIR /lp

# Copy necessary contents from first stage
COPY --from=0 /lp/etl/deploy .

# Copy necessary contents from docker data
COPY docker/linkedpipes.sh docker/updatePipelinesConfig.sh docker/pipeline.config.properties  docker/configuration.properties ./

# Grant execution permissions (just in case it is necessary)
RUN chmod a+x ./linkedpipes.sh ./executor.sh ./executor-monitor.sh ./storage.sh ./frontend.sh
