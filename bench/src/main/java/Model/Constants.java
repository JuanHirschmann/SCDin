package Model;

import java.awt.Color;

public final class Constants {

    private Constants() {

    }

    public enum commands {
        TORQUE(MEASURED_SIMULATOR_TORQUE, "Torque", "[Nm]"),
        TORQUE_COMMAND("Torque comandado", "Torque comandado", "[Nm]"),
        SPEED(MEASURED_SIMULATOR_SPEED, "Velocidad ", "[RPM]"),
        POWER(MEASURED_SIMULATOR_POWER, "Potencia activa", "[kW]"),
        CURRENT(MEASURED_SIMULATOR_CURRENT, "Corriente absoluta ", "[Arms]"),
        VOLTAGE(MEASURED_SIMULATOR_VOLTAGE, "Tensión ", "[Vrms]"),

        DUT_TORQUE(MEASURED_DUT_TORQUE, "Torque (DUT)", "[Nm]"),
        DUT_SPEED(MEASURED_DUT_SPEED, "Velocidad (DUT)", "[RPM]"),
        DUT_POWER(MEASURED_DUT_POWER, "Potencia activa (DUT)", "[kW]"),
        DUT_CURRENT(MEASURED_DUT_CURRENT, "Corriente absoluta (DUT)", "[Arms]"),
        DUT_VOLTAGE(MEASURED_DUT_VOLTAGE, "Tensión (DUT)", "[Vrms]");

        public final String varPath;
        public final String varName;
        public final String displayName;
        public final String displayUnit;
        public final String seriesName;

        commands(String name, String displayName, String displayUnit) {
            this.varPath = VAR_PATH;
            this.varName = name;
            this.displayName = displayName;
            this.displayUnit = displayUnit;
            this.seriesName = displayName + " " + displayUnit;
        }
    }

    public enum serverSideTestStatus {
        RUNNING("CORRIENDO", Color.YELLOW),
        STOPPED("PARADO", Color.ORANGE),
        ENDED("FINALIZADO", Color.GRAY),
        NOT_STARTED("NO INICIADO", Color.GRAY),
        EMERGENCY_STOP("EMERGENCIA", Color.RED, "Sistema en emergencia"),
        READY_TO_START("LISTO", Color.GRAY);

        String stateName;
        Color stateColor;
        String responseMessage;

        serverSideTestStatus(String name, Color color) {
            this.stateName = name;
            this.stateColor = color;
            this.responseMessage = "";
        }

        serverSideTestStatus(String name, Color color, String responseMessage) {
            this.stateName = name;
            this.stateColor = color;
            this.responseMessage = responseMessage;
        }

        public Color getColor() {
            return this.stateColor;
        }

        public String getResponseMessage() {
            return this.responseMessage;
        }

        public String getName() {
            return this.stateName;
        }

    }

    public enum serverSideTestError {
        SLM_NOT_ENGAGED("Módulo de línea desactivado."),
        AXIS_NOT_ENGAGED("Eje no activado."),
        EMERGENCY_STOP_ENGAGED("Parada de emergencia activada."),
        MAX_TORQUE_DERIVATIVE_EXCEEDED("Máxima derivada de cupla excedida."),
        MAX_TORQUE_VALUE_EXCEEDED("Cupla máxima excedida."),
        NO_ERROR("Sin error."),
        KEEPALIVE_FAILED("Falla de keepalive. Verifique el que el cable de red se encuentre conectado"),
        UNKNOWN_ERROR("Error desconocido.");

        String responseMessage;

        serverSideTestError(String responseMessage) {

            this.responseMessage = responseMessage;
        }

        public String getResponseMessage() {
            return this.responseMessage;
        }

    }

    public static final String VAR_PATH = "SIMOTION";
    public static final String OPERATION_MODE = "var/modeOfOperation"; // _RUN, _STOP

    public static final String TEST_DATA_PATH = "unit/LADDER.TEST_DATA.";
    public static final String TEST_STATUS = TEST_DATA_PATH + "CURRENT_STATE";
    public static final String CURRENT_ERROR = TEST_DATA_PATH + "CURRENT_ERROR";
    public static final int TORQUE_TIME_BUFFER_SIZE = 1024;
    public static final String TORQUE_EQUATION_PARAMS = TEST_DATA_PATH + "TORQUE_EQUATION.";
    public static final String TORQUE_BIAS = TORQUE_EQUATION_PARAMS + "A";
    public static final String LINEAR_COEFF = TORQUE_EQUATION_PARAMS + "B";
    public static final String QUADRATIC_COEFF = TORQUE_EQUATION_PARAMS + "C";
    public static final String INERTIA_COEFF = TORQUE_EQUATION_PARAMS + "D";

    public static final String EXTERNAL_SIGNALS_PATH = TEST_DATA_PATH + "EXTERNAL_SIGNALS.";
    public static final String TORQUE_VS_TIMESTAMP_SELECTED = EXTERNAL_SIGNALS_PATH + "TORQUE_VS_TIME_BUTTON";
    public static final String TORQUE_VS_SPEED_SELECTED = EXTERNAL_SIGNALS_PATH + "TORQUE_VS_SPEED_BUTTON";
    public static final String DUT_AXIS_ENABLE = EXTERNAL_SIGNALS_PATH + "ENABLE_SIMULATOR_AXIS";// RPM

    public static final String TEST_RUNTIME = "glob/TEST_RUNTIME";
    public static final String ENABLE_ACTIVE_LINEMODULE = EXTERNAL_SIGNALS_PATH + "ENABLE_LINEFEED";
    public static final String ENABLE_TEST_AXIS = EXTERNAL_SIGNALS_PATH + "ENABLE_TRACTION_AXIS";
    public static final String ENABLE_SIMULATOR_AXIS = EXTERNAL_SIGNALS_PATH + "ENABLE_SIMULATOR_AXIS";
    public static final String SOFTWARE_KILLSWITCH = EXTERNAL_SIGNALS_PATH + "EMERGENCY_BUTTON";
    public static final String SOFTWARE_STOP_BUTTON = EXTERNAL_SIGNALS_PATH + "STOP_BUTTON";
    public static final String SOFTWARE_START_BUTTON = EXTERNAL_SIGNALS_PATH + "START_BUTTON";
    public static final String KEEPALIVE = EXTERNAL_SIGNALS_PATH + "KEEPALIVE";
    public static final String KEEPALIVE_OVERRIDE = EXTERNAL_SIGNALS_PATH + "KEEPALIVE_OVERRIDE";
    public static final String DUT_CLEAR_TO_RECEIVE = EXTERNAL_SIGNALS_PATH + "DUT_CLEAR_TO_RECEIVE";
    public static final String DUT_SAVE_TO_BUFFER = EXTERNAL_SIGNALS_PATH + "DUT_SAVE_TO_BUFFER";
    public static final String CLEAR_TO_RECEIVE = EXTERNAL_SIGNALS_PATH + "SIM_CLEAR_TO_RECEIVE";
    public static final String SAVE_TO_BUFFER = EXTERNAL_SIGNALS_PATH + "SIM_SAVE_TO_BUFFER";
    
    public static final String SIM_AXIS_PATH = TEST_DATA_PATH + "SIMULATOR_AXIS.";
    public static final String MEASURED_SIMULATOR_SPEED = SIM_AXIS_PATH + "VARIABLES.SPEED";// RPM
    public static final String MEASURED_SIMULATOR_TORQUE = SIM_AXIS_PATH + "VARIABLES.TORQUE";// Nm
    public static final String MEASURED_SIMULATOR_VOLTAGE = SIM_AXIS_PATH + "VARIABLES.VOLTAGE";// Vrms
    public static final String MEASURED_SIMULATOR_POWER = SIM_AXIS_PATH + "VARIABLES.POWER"; // Kw
    public static final String MEASURED_SIMULATOR_CURRENT = SIM_AXIS_PATH + "VARIABLES.CURRENT"; // Kw
    public static final String AXIS_ENABLED_SIGNAL = SIM_AXIS_PATH + "IS_ENABLED";

    public static final String DUT_AXIS_PATH = TEST_DATA_PATH + "TRACTION_AXIS.";
    public static final String MEASURED_DUT_SPEED = DUT_AXIS_PATH + "VARIABLES.SPEED";// RPM
    public static final String MEASURED_DUT_TORQUE = DUT_AXIS_PATH + "VARIABLES.TORQUE";// Nm
    public static final String MEASURED_DUT_VOLTAGE = DUT_AXIS_PATH + "VARIABLES.VOLTAGE";// Vrms
    public static final String MEASURED_DUT_POWER = DUT_AXIS_PATH + "VARIABLES.POWER"; // Kw
    public static final String MEASURED_DUT_CURRENT = DUT_AXIS_PATH + "VARIABLES.CURRENT"; // Kw
    public static final String DUT_SPEED_TIME_VALUES = "glob/DUT_SPEED_VALUES";
    public static final String DUT_TIMESTAMP = "glob/DUT_TIMESTAMP";

    public static final String TORQUE_TIME_VALUES = "glob/TORQUE_VALUES";
    public static final String TORQUE_SETPOINT = "glob/TORQUE_SETPOINT";

    public static final String TIMESTAMP = "glob/TIMESTAMP";
    public static final String SIMULATOR_SPEED_LIMIT = "glob/SIMULATOR_SPEED_LIMIT";
    public static final String TORQUE_SETPOINT_COMMAND = "glob/TORQUE_VALUES";
    public static final String TIMESTAMP_COMMAND = "glob/TIMESTAMP";

    public static final String TIME_DELTA = "glob/DELTA";

    public static final int MAX_CHUNK_SIZE = 256;
    // Se indexa como string glob/TORQUE_VALUES[0],glob/TORQUE_VALUES[1]...
    public static final String AVAILABLE_TORQUE_MODES[] = { "Cupla en función del tiempo",
            "Cupla en función de la velocidad" };
    // public static final String DEFAULT_SERVER_ADDRESS =
    // "http://192.168.214.1/soap/opcxml/";
    public static final String DEFAULT_SERVER_ADDRESS = "http://169.254.11.22/soap/opcxml"; // IP SIMOSIM

    public static final int GRAPH_UPDATE_RATIO = 1;
    public static final int GRAPH_BUFFER_SIZE = 10000;
    public static final String CSV_FILEPATH = "/SCDIN/Test output/";

    /*
     * simulator_axis.actualTorque.value
     * simulator_axis.actorData.actualSpeed
     */
    public enum testTypes {
        TORQUE_VS_SPEED("Cupla en función de la velocidad"),
        TORQUE_VS_TIME("Cupla en función del tiempo"),
        MIXED_TEST("Cupla en función del tiempo y la velocidad");

        public final String displayName;

        /*
         * commands(String path,String name,String displayName)
         * {
         * this.varPath=path;
         * this.varName=name;
         * this.displayName=displayName;
         * }
         */
        testTypes(String name) {
            this.displayName = name;
        }

        @Override
        public String toString() {
            return this.displayName;
        }
    }
}
