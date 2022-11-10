FROM gradle:7.5.1-jdk17-alpine As build

ARG SOURCE_APP_PATH
ARG PARENT_BUILD_GRADLE_PATH
ARG PROTO_PATH

WORKDIR /build

COPY ${SOURCE_APP_PATH} workspace/app
COPY ${PARENT_BUILD_GRADLE_PATH} workspace/
COPY ${PROTO_PATH} proto/

# install required packages
RUN apk update && apk add gcompat binutils

RUN gradle -p workspace/app clean build

RUN jlink \
    --module-path /opt/java/openjdk/jmods \
    --add-modules $(jdeps --ignore-missing-deps --print-module-deps --multi-release 17 /build/workspace/app/build/libs/*.jar ) \
    --output jre-custom \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --compress 2

# reduce a bit more docker size (-4MB)
RUN strip -p --strip-unneeded jre-custom/lib/server/libjvm.so && \
   find jre-custom -name '*.so' | xargs -i strip -p --strip-unneeded {}

FROM alpine:latest

ARG PORT_APP
ARG JVM_ARGS
ENV ENV_PORT_APP ${PORT_APP}

WORKDIR /deployment

COPY --from=build /build/jre-custom jre-custom/
COPY --from=build /build/workspace/app/build/libs/*.jar build/app.jar

CMD jre-custom/bin/java ${JVM_ARGS} -jar build/app.jar
# CMD ["sleep","infinity"]
EXPOSE ${ENV_PORT_APP}

# docker build -t app_jlink_image .
# docker run -d --name app_jlink_container -p 3009:3009 -it app_jlink_image
# docker exec -it app_jlink_container  /bin/sh


