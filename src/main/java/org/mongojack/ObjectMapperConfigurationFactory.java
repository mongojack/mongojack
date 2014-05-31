package org.mongojack;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * 
 * This is a simple {@link ObjectMapper} Configuration Factory that can be used
 * either in code or using Spring or Aries Blueprint xml configuration.
 * <p/>
 * <b>Example Apache Aries Blueprint configuration</b>
 * <p/>
 * Setup a ObjectMapper configuration factory that configures the serialization context.
 * <pre>
 * &lt;bean id=&quot;jacksonConfigurationFactory&quot; class=&quot;org.mongojack.ObjectMapperConfigurationFactory&quot;&gt;
 *   &lt;property name=&quot;serializationInclusion&quot;&gt;
 *     &lt;value type=&quot;com.fasterxml.jackson.annotation.JsonInclude.Include&quot;&gt;NON_NULL&lt;/value&gt;
 *   &lt;/property&gt;
 *   &lt;property name=&quot;disableSerializationFeatures&quot;&gt;
 *     &lt;list&gt;
 *       &lt;bean class=&quot;com.fasterxml.jackson.databind.SerializationFeature&quot; factory-method=&quot;valueOf&quot;&gt;
 *         &lt;argument value=&quot;WRITE_DATES_AS_TIMESTAMPS&quot; /&gt;
 *       &lt;/bean&gt;
 *       &lt;bean class=&quot;com.fasterxml.jackson.databind.SerializationFeature&quot; factory-method=&quot;valueOf&quot;&gt;
 *         &lt;argument value=&quot;WRAP_ROOT_VALUE&quot; /&gt;
 *       &lt;/bean&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * Then create the configured ObjectMapper with JodaModule and MongoJackModule and all serilaisation context settings defined in the factory.
 * <pre>
 * &lt;bean id=&quot;mongojackObjectMapper&quot; factory-ref=&quot;jacksonConfigurationFactory&quot; factory-method=&quot;createMapper&quot;&gt;
 *   &lt;argument&gt;
 *     &lt;list value-type=&quot;com.fasterxml.jackson.databind.Module&quot;&gt;
 *       &lt;bean class=&quot;org.mongojack.MongoJackModule&quot; /&gt;
 *       &lt;bean class=&quot;com.fasterxml.jackson.datatype.joda.JodaModule&quot; /&gt;
 *     &lt;/list&gt;
 *   &lt;/argument&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * @author Niels Bertram
 * 
 */
public class ObjectMapperConfigurationFactory {

	private DateFormat dateFormat = null;

	private Include serializationInclusion = Include.ALWAYS; // the default

	private List<SerializationFeature> enableSerializationFeatures = null;

	private List<SerializationFeature> disableSerializationFeatures = null;

	private List<Module> modules = null;

	public DateFormat getDateFormat() {
		return this.dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public Include getSerializationInclusion() {
		return serializationInclusion;
	}

	public void setSerializationInclusion(Include serializationInclusion) {
		this.serializationInclusion = serializationInclusion;
	}

	public List<SerializationFeature> getEnableSerializationFeatures() {
		return enableSerializationFeatures;
	}

	public void setEnableSerializationFeatures(
			List<SerializationFeature> enableSerializationFeatures) {
		this.enableSerializationFeatures = enableSerializationFeatures;
	}

	public void setEnableSerializationFeatures(SerializationFeature... features) {
		if (this.enableSerializationFeatures == null)
			this.enableSerializationFeatures = Arrays.asList(features);
		else
			this.enableSerializationFeatures.addAll(Arrays.asList(features));
	}

	public List<SerializationFeature> getDisableSerializationFeatures() {
		return disableSerializationFeatures;
	}

	public void setDisableSerializationFeatures(
			List<SerializationFeature> disableSerializationFeatures) {
		this.disableSerializationFeatures = disableSerializationFeatures;
	}

	public void setDisableSerializationFeatures(
			SerializationFeature... features) {
		if (this.disableSerializationFeatures == null)
			this.disableSerializationFeatures = Arrays.asList(features);
		else
			this.disableSerializationFeatures.addAll(Arrays.asList(features));
	}

	public List<Module> getModules() {
		return modules;
	}

	public void setModules(List<Module> modules) {
		addModules(true, modules);
	}

	public void setModules(Module... module) {
		addModules(true, Arrays.asList(module));
	}

	public void addModules(List<Module> modules) {
		addModules(false, modules);
	}

	public void addModules(Module... module) {
		addModules(false, Arrays.asList(module));
	}

	private void addModules(boolean clearCurrent, List<Module> modules) {
		if (!clearCurrent && this.modules != null) {
			this.modules.addAll(modules);
		} else {
			this.modules = modules;
		}
	}

	/**
	 * A factory method to construct the actual ObjectMapper class with all the
	 * configuration specified in the factory.
	 * 
	 * @return a configured ObjectMapper
	 */
	public ObjectMapper createMapper() {

		ObjectMapper mapper = new ObjectMapper();

		// set a suitable date format
		if (this.dateFormat != null) {
			mapper.setDateFormat(dateFormat);
		}

		// register modules
		if (this.modules != null) {
			for (Module module : this.modules) {
				mapper.registerModule(module);
			}
		}

		// set serialization type
		if (this.serializationInclusion != null) {
			mapper.setSerializationInclusion(this.serializationInclusion);
		}

		// disable all serialization features provided
		if (this.disableSerializationFeatures != null) {
			for (SerializationFeature feature : this.disableSerializationFeatures) {
				mapper.disable(feature);
			}
		}

		// enable all serialization features provided
		if (this.enableSerializationFeatures != null) {
			for (SerializationFeature feature : this.enableSerializationFeatures) {
				mapper.enable(feature);
			}
		}

		return mapper;
	}

	/**
	 * 
	 * A factory method to construct the actual ObjectMapper class with all the
	 * serialization configuration specified in the factory. The modules that
	 * have been originally provided to the factory are replaced with the
	 * modules provided to this function. Please note after this call the
	 * original modules will be replaced by the modules provided to the factory.
	 * 
	 * @param modules
	 *            the new modules to use creating the ObjectMapper
	 * 
	 * @return a configured ObjectMapper
	 */
	public ObjectMapper createMapper(Module... modules) {
		setModules(modules);
		return createMapper();
	}

}
