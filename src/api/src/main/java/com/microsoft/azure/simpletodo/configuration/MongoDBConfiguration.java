/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.simpletodo.configuration;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class MongoDBConfiguration {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(
            Arrays.asList(new OffsetDateTimeReadConverter(), new OffsetDateTimeWriteConverter())
        );
    }

    static class OffsetDateTimeWriteConverter implements Converter<OffsetDateTime, Date> {

        @Override
        public Date convert(OffsetDateTime source) {
            return Date.from(source.toInstant().atZone(ZoneOffset.UTC).toInstant());
        }
    }

    static class OffsetDateTimeReadConverter implements Converter<Date, OffsetDateTime> {

        @Override
        public OffsetDateTime convert(Date source) {
            return source.toInstant().atOffset(ZoneOffset.UTC);
        }
    }

    @Configuration
    @Profile("local")
    class EmbeddedMongoServer {
        @Bean
        public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory) {
            return new MongoTemplate(mongoDbFactory);
        }

        @Bean
        public MongoDatabaseFactory mongoDbFactory(MongoServer mongoServer) {
            String connectionString = mongoServer.getConnectionString();
            return new SimpleMongoClientDatabaseFactory(connectionString + "/test");
        }

        @Bean(destroyMethod = "shutdown")
        public MongoServer mongoServer() {
            MongoServer mongoServer = new MongoServer(new MemoryBackend());
            mongoServer.bind();
            return mongoServer;
        }
    }

}
