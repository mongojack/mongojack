package org.mongojack;

import com.fasterxml.jackson.databind.cfg.MapperConfig;

@SuppressWarnings("unused")
public class MongoJackModuleConfiguration {

    private final int moduleFeatures;

    public MongoJackModuleConfiguration() {
        moduleFeatures = MapperConfig.collectFeatureDefaults(MongoJackModuleFeature.class);
    }

    public MongoJackModuleConfiguration(final int moduleFeatures) {
        this.moduleFeatures = moduleFeatures;
    }

    public final boolean isEnabled(MongoJackModuleFeature f) {
        return (moduleFeatures & f.getMask()) != 0;
    }

    /**
     * Fluent factory method that will construct and return a new configuration
     * object instance with specified features enabled.
     */
    public MongoJackModuleConfiguration with(MongoJackModuleFeature feature) {
        int newModuleFeatures = (moduleFeatures | feature.getMask());
        return (newModuleFeatures == moduleFeatures) ? this :
            new MongoJackModuleConfiguration(newModuleFeatures);
    }

    /**
     * Fluent factory method that will construct and return a new configuration
     * object instance with specified features enabled.
     */
    public MongoJackModuleConfiguration with(
        MongoJackModuleFeature first,
        MongoJackModuleFeature... features
    ) {
        int newModuleFeatures = moduleFeatures | first.getMask();
        for (MongoJackModuleFeature f : features) {
            newModuleFeatures |= f.getMask();
        }
        return (newModuleFeatures == moduleFeatures) ? this :
            new MongoJackModuleConfiguration(newModuleFeatures);
    }

    /**
     * Fluent factory method that will construct and return a new configuration
     * object instance with specified features enabled.
     */
    public MongoJackModuleConfiguration withFeatures(MongoJackModuleFeature... features) {
        int newModuleFeatures = moduleFeatures;
        for (MongoJackModuleFeature f : features) {
            newModuleFeatures |= f.getMask();
        }
        return (newModuleFeatures == moduleFeatures) ? this :
            new MongoJackModuleConfiguration(newModuleFeatures);
    }

    /**
     * Fluent factory method that will construct and return a new configuration
     * object instance with specified feature disabled.
     */
    public MongoJackModuleConfiguration without(MongoJackModuleFeature feature) {
        int newModuleFeatures = moduleFeatures & ~feature.getMask();
        return (newModuleFeatures == moduleFeatures) ? this :
            new MongoJackModuleConfiguration(newModuleFeatures);
    }

    /**
     * Fluent factory method that will construct and return a new configuration
     * object instance with specified features disabled.
     */
    public MongoJackModuleConfiguration without(
        MongoJackModuleFeature first,
        MongoJackModuleFeature... features
    ) {
        int newModuleFeatures = moduleFeatures & ~first.getMask();
        for (MongoJackModuleFeature f : features) {
            newModuleFeatures &= ~f.getMask();
        }
        return (newModuleFeatures == moduleFeatures) ? this :
            new MongoJackModuleConfiguration(newModuleFeatures);
    }

    /**
     * Fluent factory method that will construct and return a new configuration
     * object instance with specified features disabled.
     */
    public MongoJackModuleConfiguration withoutFeatures(MongoJackModuleFeature... features) {
        int newModuleFeatures = moduleFeatures;
        for (MongoJackModuleFeature f : features) {
            newModuleFeatures &= ~f.getMask();
        }
        return (newModuleFeatures == moduleFeatures) ? this :
            new MongoJackModuleConfiguration(newModuleFeatures);
    }

}
