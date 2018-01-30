package io.mainflux.loadmanager.engine;

public enum Platform {
    OSGP {
        @Override
        PlatformClient client() {
            return new OSGP();
        }
    },

    MAINFLUX {
        @Override
        PlatformClient client() {
            return new Mainflux();
        }
    };

    abstract PlatformClient client();
}
