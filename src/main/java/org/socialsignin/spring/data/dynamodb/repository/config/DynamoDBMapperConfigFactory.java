package org.socialsignin.spring.data.dynamodb.repository.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

public class DynamoDBMapperConfigFactory implements FactoryBean<DynamoDBMapperConfig>, BeanPostProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBMapperConfigFactory.class);
	@Override
	public DynamoDBMapperConfig getObject() throws Exception {
		return DynamoDBMapperConfig.DEFAULT;
	}

	@Override
	public Class<?> getObjectType() {
		return DynamoDBMapperConfig.class;
	}

	@Nullable
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof DynamoDBMapperConfig) {
			DynamoDBMapperConfig dynamoDBMapperConfig = (DynamoDBMapperConfig) bean;
			if (dynamoDBMapperConfig == DynamoDBMapperConfig.DEFAULT) {
				return bean;
			}
			// #146, #81 #157
			// Trying to fix half-initialized DynamoDBMapperConfigs here.
			// The old documentation advised to start with an empty builder. Therefore we
			// try here to set required fields to their defaults -
			// As the documentation at
			// https://github.com/derjust/spring-data-dynamodb/wiki/Alter-table-name-during-runtime
			// (same as https://git.io/DynamoDBMapperConfig)
			// now does: Start with #DEFAULT and add what's required
			DynamoDBMapperConfig.Builder emptyBuilder = DynamoDBMapperConfig.builder(); // empty (!) builder

			if (dynamoDBMapperConfig.getConversionSchema() == null) {
				LOGGER.warn(
						"No ConversionSchema set in the provided dynamoDBMapperConfig! Merging with DynamoDBMapperConfig.DEFAULT - Please see https://git.io/DynamoDBMapperConfig");
				// DynamoDBMapperConfig#DEFAULT comes with a ConversionSchema
				emptyBuilder.withConversionSchema(DynamoDBMapperConfig.DEFAULT.getConversionSchema());
			}

			if (dynamoDBMapperConfig.getTypeConverterFactory() == null) {
				LOGGER.warn(
						"No TypeConverterFactory set in the provided dynamoDBMapperConfig! Merging with DynamoDBMapperConfig.DEFAULT - Please see https://git.io/DynamoDBMapperConfig");
				// DynamoDBMapperConfig#DEFAULT comes with a TypeConverterFactory
				emptyBuilder.withTypeConverterFactory(DynamoDBMapperConfig.DEFAULT.getTypeConverterFactory());
			}

			// Deprecated but the only way how DynamoDBMapperConfig#merge is exposed
			return new DynamoDBMapperConfig(dynamoDBMapperConfig, emptyBuilder.build());
			
		} else {
			return bean;
		}
	}
}
