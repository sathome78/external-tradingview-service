FROM java:8
ARG APP_PATH=/opt/tradingview
ARG ENVIRONMENT
ARG PORT

WORKDIR ${APP_PATH}
COPY ./target/tradingview.jar tradingview.jar
VOLUME ${APP_PATH}/indexes
RUN sh -c 'touch tradingview.jar'

ARG CONFIG_ACTIVE_PROFILE="-Dspring.profiles.active="${ENVIRONMENT}

EXPOSE ${PORT}

CMD java -jar tradingview.jar ${CONFIG_ACTIVE_PROFILE}