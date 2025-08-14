package Views;

import static Views.Constants.BROWSE_FILE_BUTTON_LABEL;
import static Views.Constants.CONNECT_BUTTON_LABEL;
import static Views.Constants.CSV_FILEPATH;
import static Views.Constants.EMERGENCY_STOP_BUTTON_LABEL;
import static Views.Constants.FILE_EXCEEDS_MAX_TORQUE_MSG;
import static Views.Constants.FILE_EXCEEDS_MIN_COMMAND_DELTA_MSG;
import static Views.Constants.LARGE_BIAS_MSG;
import static Views.Constants.LARGE_INERTIA_MSG;
import static Views.Constants.MAX_SPEED;
import static Views.Constants.MAX_TORQUE;
import static Views.Constants.MIN_COMMAND_DELTA;
import static Views.Constants.NEGATIVE_INERTIA_MSG;
import static Views.Constants.NO_FILE_SELECTED_MSG;
import static Views.Constants.PAUSE_BUTTON_LABEL;
import static Views.Constants.POWER_ON_BUTTON_LABEL;
import static Views.Constants.PRE_START_WARNING;
import static Views.Constants.SELF_SUSTAINED_MODE_WARNING;
import static Views.Constants.SELF_SUSTAINED_NO_FILE_SELECTED_MSG;
import static Views.Constants.SELF_SUSTAINED_TEST_IMPORT_LABEL;
import static Views.Constants.SHUTDOWN_BUTTON_LABEL;
import static Views.Constants.START_BUTTON_LABEL;
import static Views.Constants.UNKNOWN_ENDTIME;
import static Views.Constants.UNKNOWN_FILE_FORMAT_MSG;
import static Views.Constants.UNKNOWN_PARAMETER_FORMAT_MSG;
import static Views.Constants.SET_TEST_PARAMETERS_BUTTON_LABEL;
import static Views.Constants.WRITE_CSV;
import static Views.Constants.SELF_SUSTAINED_TEST_LABEL;
import static Views.Constants.RESUME_BUTTON_LABEL;
import static Views.Constants.SAVEFILE_NAME_DATE_FORMAT;
import static Views.Constants.SELF_SUSTAINED_FILE_EXCEEDS_MAX_SPEED_MSG;
import static Views.Constants.SELF_SUSTAINED_FILE_EXCEEDS_MIN_COMMAND_DELTA_MSG;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JDialog;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import Views.Constants.testStates;
import Model.Constants.testTypes;
import Swing.MainFrame;
import Swing.SelfSustainedTestFrame;
import Swing.TorqueEquationParameter;

import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.io.BufferedWriter;
import java.io.File;

import Controller.Controller;
import Controller.MeasurementBuffer;
import Controller.TorqueTimeValues;
import Controller.ViewListener;
import Model.Constants.commands;
import Model.Constants.serverSideTestStatus;

public class Views implements ViewListener {

    Controller appController = null;
    private boolean popupVisible = false;
    private MainFrame frame = new MainFrame();
    private SelfSustainedTestFrame selfSustaintedFrame = new SelfSustainedTestFrame();
    private SwingPlotWorker plotUpdater = new SwingPlotWorker();
    private JDialog dialog = new JDialog();

    /**
     * @param controller
     */
    public void setController(Controller controller) {
        appController = controller;
    };

    /**
     * @param controller
     */
    public boolean DUTModeSelected() {
        return this.frame.getInputPanel().selfSustainedTestSelection.isSelected();
    };

    /**
     * Returns controller instance
     * 
     * @return Controller
     */
    public Controller getController() {
        return this.appController;
    }

    private Map<String, XYSeriesCollection> mainDataset = new Hashtable<>();
    private Map<String, XYSeriesCollection> selfSustainedDataset = new Hashtable<>();

    private XYSeries torqueData = new XYSeries(commands.TORQUE.seriesName);
    private XYSeries speedData = new XYSeries(commands.SPEED.seriesName);
    private XYSeries voltageData = new XYSeries(commands.VOLTAGE.seriesName);
    private XYSeries powerData = new XYSeries(commands.POWER.seriesName);
    private XYSeries currentData = new XYSeries(commands.CURRENT.seriesName);
    private XYSeries torqueCommandData = new XYSeries(commands.TORQUE_COMMAND.seriesName);

    private XYSeries selfSustainedTorqueData = new XYSeries(commands.DUT_TORQUE.seriesName);
    private XYSeries selfSustainedSpeedData = new XYSeries(commands.DUT_SPEED.seriesName);
    private XYSeries selfSustainedVoltageData = new XYSeries(commands.DUT_VOLTAGE.seriesName);
    private XYSeries selfSustainedPowerData = new XYSeries(commands.DUT_POWER.seriesName);
    private XYSeries selfSustainedCurrentData = new XYSeries(commands.DUT_CURRENT.seriesName);

    /**
     * Actualiza las mediciones en pantalla, agrega la unidad de medida al final de
     * la
     * Cadena de texto
     * 
     * @param simulator_torque  Torque medido en Nm
     * @param simulator_speed   velocidad medida en RPM
     * @param simulator_current Corriente medida en A
     * @param simulator_voltage Tensión medida en V
     * @param simulator_power   Potencia medida en W
     */

    private class SwingPlotWorker extends SwingWorker<Boolean, MeasurementBuffer> {
        private static int DISPLAYED_MEASUREMENTS_UPDATE_RATIO = 5;// Cada cuanto chunks actualiza mediciones
        private int chunkCounter = 0;
        private static int MEAN_SAMPLE_SIZE = 2;

        @Override
        protected Boolean doInBackground() throws Exception {

            while (!isCancelled()) {
                if (!getController().getMeasurementBuffer().isEmpty()) {
                    MeasurementBuffer buffer = new MeasurementBuffer(getController().getMeasurementBuffer());
                    publish(buffer);
                    getController().clearMeasurementBuffer();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // eat it. caught when interrupt is called
                        System.out.println("MySwingWorker shut down.");
                    }
                }

            }
            return true;

        }

        @Override
        protected void process(List<MeasurementBuffer> chunks) {

            for (MeasurementBuffer data : chunks) {

                for (String key : data.getKeySet()) {
                    ArrayList<Float> value = new ArrayList<Float>(data.getBufferedData(key));
                    ArrayList<Float> timestamp = new ArrayList<Float>(data.getBufferedDataTimestamp(key));

                    for (int i = 0; i < value.size(); i++) {
                        if (key == "speed") {
                            mainDataset.get(key).getSeries(key).add(timestamp.get(i), value.get(i));
                        } else {
                            try {

                                mainDataset.get(key).getSeries(key).add(timestamp.get(i), value.get(i));
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                System.out.println(key);
                                selfSustainedDataset.get(key).getSeries(key).add(timestamp.get(i), value.get(i));
                            }

                        }

                    }

                }
                chunkCounter++;
                if (chunkCounter == DISPLAYED_MEASUREMENTS_UPDATE_RATIO) {
                    chunkCounter = 0;
                    for (String series : mainDataset.keySet()) {
                        float accumValue = 0;
                        int seriesLength = 0;
                        seriesLength = mainDataset.get(series).getSeries(series).getItemCount();
                        int sampleSize = seriesLength;
                        if (seriesLength > MEAN_SAMPLE_SIZE) {
                            sampleSize = MEAN_SAMPLE_SIZE;
                        }
                        for (int i = 0; i < sampleSize; i++) {
                            accumValue += mainDataset.get(series).getSeries(series).getY(seriesLength - i - 1)
                                    .floatValue();
                        }

                        frame.getInputPanel().displayedMeasurements.addMeasurement(series,
                                accumValue / sampleSize);
                        accumValue = 0;
                    }
                    ;
                }

            }

        }

    }

    public Views() {

        this.setup();

        this.torqueVsTimeVisibility(true);

    }

    /**
     * Updates external's buffer load elements
     * 
     * @param currentValue current elements loaded on buffer
     * @param finalValue   total elements to load
     */
    public void updateTorqueTimeLoad(int currentValue, int finalValue) {
        String output = String.valueOf(currentValue) + " / " + String.valueOf(finalValue);
        frame.getInputPanel().itemsLoadedLabel.setText(output);

    }

    /**
     * Updates the connection to PLC status
     * 
     * @param isConnected
     */
    public void updateConnectionStatus(boolean isConnected) {
        if (isConnected) {

            frame.getInputPanel().connectionIndicator.green();
        } else {

            frame.getInputPanel().connectionIndicator.red();
        }
    }

    /**
     * Updates server-side test status on semaphore indicator
     * 
     * @param testState serverSideTestStatus (NOT_STARTED, RUNNING, STOPPED....)
     */
    public void updateTestStatus(serverSideTestStatus testState) {
        frame.getInputPanel().semaphoreIndicator.setColor(testState.getColor());
        frame.getInputPanel().semaphoreIndicator.setText(testState.getName());

        if (testState == serverSideTestStatus.STOPPED) {
            frame.getInputPanel().startButton.setText(RESUME_BUTTON_LABEL);
        } else if (testState == serverSideTestStatus.RUNNING) {
            frame.getInputPanel().startButton.setText(PAUSE_BUTTON_LABEL);
        } else {

            frame.getInputPanel().startButton.setText(START_BUTTON_LABEL);
        }
    }

    /**
     * Changes power connection power indicator
     * 
     * @param isConnected
     */
    public void updateALMStatus(boolean isConnected) {
        if (isConnected) {

            frame.getInputPanel().powerOnIndicator.green();
        } else {

            frame.getInputPanel().powerOnIndicator.red();
        }
    }

    /**
     * updates loaded test status on test indicator.
     * 
     * @param isLoaded
     */
    public void updateLoadedTestStatus(boolean isLoaded) {
        if (isLoaded) {

            frame.getInputPanel().loadedTestIndicator.green();
        } else {

            frame.getInputPanel().loadedTestIndicator.red();
        }
    }

    /*
     * Set up of main frame panels.
     */
    private void setup() {
        frame.getInputPanel().startButton.addActionListener(new ButtonHandler());
        frame.getInputPanel().emergencyButton.addActionListener(new ButtonHandler());
        frame.getInputPanel().buttonConnect.addActionListener(new ButtonHandler());
        frame.getInputPanel().powerOnButton.addActionListener(new ButtonHandler());
        frame.getInputPanel().shutdownButton.addActionListener(new ButtonHandler());
        frame.getInputPanel().setParametersButton.addActionListener(new ButtonHandler());
        frame.getInputPanel().saveCSVButton.addActionListener(new ButtonHandler());
        frame.getInputPanel().openFileButton.addActionListener(new ButtonHandler());
        frame.getInputPanel().torqueTestModeComboBox.addItemListener(new TestTypeHandler());
        frame.getInputPanel().testPeriodsSpinner.addChangeListener(new PeriodExtensionHandler());

        frame.getInputPanel().selfSustainedTestSelection.addActionListener(new ButtonHandler());
        frame.getInputPanel().openDUTFileButton.addActionListener(new ButtonHandler());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createChart();
        createSelfSustainedChart();
        this.blockInput(testStates.INITIAL);

    }

    /**
     * Blocks user access considering current test step and input error
     * 
     * @param currentStep
     */
    private void blockInput(testStates currentStep) {

        if (currentStep == testStates.INITIAL) {
            frame.getInputPanel().buttonConnect.setEnabled(true);
            frame.getInputPanel().powerOnButton.setEnabled(false);
            frame.getInputPanel().startButton.setEnabled(false);
            frame.getInputPanel().emergencyButton.setEnabled(false);
            frame.getInputPanel().shutdownButton.setEnabled(true);
            frame.getInputPanel().targetIP.setEnabled(true);
            frame.getInputPanel().variablesPanel.setEnabled(false);

        } else if (currentStep == testStates.PLC_CONNECTED) {
            frame.getInputPanel().buttonConnect.setEnabled(false);
            frame.getInputPanel().powerOnButton.setEnabled(true);
            frame.getInputPanel().startButton.setEnabled(false);
            frame.getInputPanel().emergencyButton.setEnabled(true);
            frame.getInputPanel().shutdownButton.setEnabled(true);
            frame.getInputPanel().targetIP.setEnabled(false);

            frame.getInputPanel().variablesPanel.setEnabled(false);
        } else if (currentStep == testStates.POWER_CONNECTED) {

            frame.getInputPanel().buttonConnect.setEnabled(false);
            frame.getInputPanel().powerOnButton.setEnabled(false);
            frame.getInputPanel().startButton.setEnabled(false);
            frame.getInputPanel().emergencyButton.setEnabled(true);
            frame.getInputPanel().shutdownButton.setEnabled(true);
            frame.getInputPanel().targetIP.setEnabled(false);

            frame.getInputPanel().variablesPanel.setEnabled(true);
        } else if (currentStep == testStates.TEST_PARAMETER_LOAD) {
            // Tengo que asegurar que no hayan metido cualquier cosa
            frame.getInputPanel().buttonConnect.setEnabled(false);
            frame.getInputPanel().powerOnButton.setEnabled(false);
            frame.getInputPanel().startButton.setEnabled(false);
            frame.getInputPanel().emergencyButton.setEnabled(true);
            frame.getInputPanel().shutdownButton.setEnabled(true);
            frame.getInputPanel().targetIP.setEnabled(false);

            frame.getInputPanel().variablesPanel.setEnabled(true);

        } else if (currentStep == testStates.TEST_PARAMETER_READY) {
            frame.getInputPanel().buttonConnect.setEnabled(false);
            frame.getInputPanel().powerOnButton.setEnabled(false);
            frame.getInputPanel().startButton.setEnabled(true);
            frame.getInputPanel().emergencyButton.setEnabled(true);
            frame.getInputPanel().shutdownButton.setEnabled(true);
            frame.getInputPanel().targetIP.setEnabled(false);

            frame.getInputPanel().variablesPanel.setEnabled(false);

        } else if (currentStep == testStates.TEST_RUNNING) {
            frame.getInputPanel().buttonConnect.setEnabled(false);
            frame.getInputPanel().powerOnButton.setEnabled(false);
            frame.getInputPanel().startButton.setEnabled(true);
            frame.getInputPanel().emergencyButton.setEnabled(true);
            frame.getInputPanel().shutdownButton.setEnabled(true);
            frame.getInputPanel().targetIP.setEnabled(false);

            frame.getInputPanel().variablesPanel.setEnabled(false);

        } else if (currentStep == testStates.TEST_END) {
            frame.getInputPanel().buttonConnect.setEnabled(true);
            frame.getInputPanel().powerOnButton.setEnabled(false);
            frame.getInputPanel().startButton.setEnabled(true);
            frame.getInputPanel().emergencyButton.setEnabled(false);
            frame.getInputPanel().shutdownButton.setEnabled(true);
            frame.getInputPanel().targetIP.setEnabled(false);

            frame.getInputPanel().variablesPanel.setEnabled(false);
            frame.getInputPanel().saveCSVButton.setEnabled(true);
        }

    }

    /**
     * Creates Chart plot
     * 
     * @return JFreeChart
     */
    private JFreeChart createChart() {

        mainDataset.put(commands.TORQUE.seriesName, new XYSeriesCollection());
        mainDataset.put(commands.VOLTAGE.seriesName, new XYSeriesCollection());
        mainDataset.put(commands.SPEED.seriesName, new XYSeriesCollection());
        mainDataset.put(commands.CURRENT.seriesName, new XYSeriesCollection());
        mainDataset.put(commands.POWER.seriesName, new XYSeriesCollection());

        mainDataset.get(commands.TORQUE.seriesName).addSeries(torqueData);
        mainDataset.get(commands.TORQUE.seriesName).addSeries(torqueCommandData);
        mainDataset.get(commands.VOLTAGE.seriesName).addSeries(voltageData);
        mainDataset.get(commands.SPEED.seriesName).addSeries(speedData);
        mainDataset.get(commands.CURRENT.seriesName).addSeries(currentData);
        mainDataset.get(commands.POWER.seriesName).addSeries(powerData);

        // construct the plot
        XYPlot topPlot = new XYPlot();
        topPlot.setDataset(0, mainDataset.get(commands.TORQUE.seriesName));
        topPlot.setDataset(1, mainDataset.get(commands.SPEED.seriesName));
        topPlot.setRenderer(0, new XYLineAndShapeRenderer(true, false));
        topPlot.setRenderer(1, new XYLineAndShapeRenderer(true, false));
        topPlot.setRenderer(2, new XYLineAndShapeRenderer(true, false));
        topPlot.getRenderer(0).setSeriesStroke(0, new BasicStroke(2f));
        topPlot.getRenderer(0).setSeriesStroke(1, new BasicStroke(2f));
        topPlot.getRenderer(1).setSeriesStroke(0, new BasicStroke(2f));

        /*
         * renderer.setSeriesStroke(1, new BasicStroke(2f));
         * XYItemRenderer renderer2=topPlot.getRenderer(1);
         * renderer2.setSeriesStroke(0, new BasicStroke(2f));
         */

        topPlot.setRangeAxis(0, new NumberAxis("Torque [Nm]"));
        topPlot.setRangeAxis(1, new NumberAxis("Velocidad [RPM]"));

        topPlot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
        topPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        topPlot.setDomainAxis(new NumberAxis("Tiempo[ms]"));

        topPlot.mapDatasetToRangeAxis(0, 0);
        topPlot.mapDatasetToRangeAxis(1, 1);
        // customize the plot with renderers and axis
        XYPlot bottomPlot = new XYPlot();
        bottomPlot.setDataset(0, mainDataset.get(commands.VOLTAGE.seriesName));
        bottomPlot.setDataset(1, mainDataset.get(commands.CURRENT.seriesName));
        bottomPlot.setDataset(2, mainDataset.get(commands.POWER.seriesName));
        bottomPlot.setRenderer(0, new XYLineAndShapeRenderer(true, false));
        bottomPlot.getRenderer(0).setSeriesStroke(0, new BasicStroke(2f));

        bottomPlot.setRenderer(1, new XYLineAndShapeRenderer(true, false));
        bottomPlot.getRenderer(1).setSeriesStroke(0, new BasicStroke(2f));

        bottomPlot.setRenderer(2, new XYLineAndShapeRenderer(true, false));
        bottomPlot.getRenderer(2).setSeriesStroke(0, new BasicStroke(2f));

        bottomPlot.setRangeAxis(0, new NumberAxis("Tensión [Vrms]"));
        bottomPlot.setRangeAxis(1, new NumberAxis("Corriente [Arms]"));
        bottomPlot.setRangeAxis(2, new NumberAxis("Potencia [KW]"));
        bottomPlot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
        bottomPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        bottomPlot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_RIGHT);
        bottomPlot.mapDatasetToRangeAxis(0, 0);
        bottomPlot.mapDatasetToRangeAxis(1, 1);
        bottomPlot.mapDatasetToRangeAxis(2, 2);
        bottomPlot.setDomainAxis(new NumberAxis("Tiempo[ms]"));

        // generate the chart
        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis("Tiempo [ms]"));
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.add(topPlot);
        plot.add(bottomPlot);
        JFreeChart chart = new JFreeChart("Ensayo en tiempo real", null, plot, true);
        frame.setChart(chart);
        return chart;
    }

    /**
     * Creates Chart plot
     * 
     * @return JFreeChart
     */
    private JFreeChart createSelfSustainedChart() {

        selfSustainedDataset.put(commands.DUT_TORQUE.seriesName, new XYSeriesCollection());
        selfSustainedDataset.put(commands.DUT_VOLTAGE.seriesName, new XYSeriesCollection());
        selfSustainedDataset.put(commands.DUT_SPEED.seriesName, new XYSeriesCollection());
        selfSustainedDataset.put(commands.DUT_CURRENT.seriesName, new XYSeriesCollection());
        selfSustainedDataset.put(commands.DUT_POWER.seriesName, new XYSeriesCollection());

        selfSustainedDataset.get(commands.DUT_TORQUE.seriesName).addSeries(selfSustainedTorqueData);
        selfSustainedDataset.get(commands.DUT_VOLTAGE.seriesName).addSeries(selfSustainedVoltageData);
        selfSustainedDataset.get(commands.DUT_SPEED.seriesName).addSeries(selfSustainedSpeedData);
        selfSustainedDataset.get(commands.DUT_CURRENT.seriesName).addSeries(selfSustainedCurrentData);
        selfSustainedDataset.get(commands.DUT_POWER.seriesName).addSeries(selfSustainedPowerData);

        // construct the plot
        XYPlot topPlot = new XYPlot();
        topPlot.setDataset(0, selfSustainedDataset.get(commands.DUT_TORQUE.seriesName));
        topPlot.setDataset(1, selfSustainedDataset.get(commands.DUT_SPEED.seriesName));
        topPlot.setRenderer(0, new XYLineAndShapeRenderer(true, false));
        topPlot.setRenderer(1, new XYLineAndShapeRenderer(true, false));
        topPlot.setRenderer(2, new XYLineAndShapeRenderer(true, false));
        topPlot.getRenderer(0).setSeriesStroke(0, new BasicStroke(2f));
        topPlot.getRenderer(0).setSeriesStroke(1, new BasicStroke(2f));
        topPlot.getRenderer(1).setSeriesStroke(0, new BasicStroke(2f));

        /*
         * renderer.setSeriesStroke(1, new BasicStroke(2f));
         * XYItemRenderer renderer2=topPlot.getRenderer(1);
         * renderer2.setSeriesStroke(0, new BasicStroke(2f));
         */

        topPlot.setRangeAxis(0, new NumberAxis("Torque [Nm]"));
        topPlot.setRangeAxis(1, new NumberAxis("Velocidad [RPM]"));

        topPlot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
        topPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        topPlot.setDomainAxis(new NumberAxis("Tiempo[ms]"));

        topPlot.mapDatasetToRangeAxis(0, 0);
        topPlot.mapDatasetToRangeAxis(1, 1);
        // customize the plot with renderers and axis
        XYPlot bottomPlot = new XYPlot();
        bottomPlot.setDataset(0, selfSustainedDataset.get(commands.DUT_VOLTAGE.seriesName));
        bottomPlot.setDataset(1, selfSustainedDataset.get(commands.DUT_CURRENT.seriesName));
        bottomPlot.setDataset(2, selfSustainedDataset.get(commands.DUT_POWER.seriesName));
        bottomPlot.setRenderer(0, new XYLineAndShapeRenderer(true, false));
        bottomPlot.getRenderer(0).setSeriesStroke(0, new BasicStroke(2f));

        bottomPlot.setRenderer(1, new XYLineAndShapeRenderer(true, false));
        bottomPlot.getRenderer(1).setSeriesStroke(0, new BasicStroke(2f));

        bottomPlot.setRenderer(2, new XYLineAndShapeRenderer(true, false));
        bottomPlot.getRenderer(2).setSeriesStroke(0, new BasicStroke(2f));

        bottomPlot.setRangeAxis(0, new NumberAxis("Tensión [Vrms]"));
        bottomPlot.setRangeAxis(1, new NumberAxis("Corriente [Arms]"));
        bottomPlot.setRangeAxis(2, new NumberAxis("Potencia [KW]"));
        bottomPlot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
        bottomPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        bottomPlot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_RIGHT);
        bottomPlot.mapDatasetToRangeAxis(0, 0);
        bottomPlot.mapDatasetToRangeAxis(1, 1);
        bottomPlot.mapDatasetToRangeAxis(2, 2);
        bottomPlot.setDomainAxis(new NumberAxis("Tiempo[ms]"));

        // generate the chart
        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis("Tiempo [ms]"));
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.add(topPlot);
        plot.add(bottomPlot);
        JFreeChart chart = new JFreeChart("Ensayo en tiempo real (DUT)", null, plot, true);
        selfSustaintedFrame.setChart(chart);
        return chart;
    }

    /**
     * Handles period extension for CSV waveform input
     */
    private class PeriodExtensionHandler implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (getController().getTorqueTimeValues().length() != 0) {

                getController().extendTorqueTimeValues((int) frame.getInputPanel().testPeriodsSpinner.getValue());

                plotTorqueTime();
            }
        }
    }

    /**
     * set user input visibility for torque vs time test commands
     * 
     * @param visible
     */
    public void torqueVsTimeVisibility(boolean visible) {
        frame.getInputPanel().stopTime.setVisible(!visible);
        frame.getInputPanel().filename.setVisible(visible);
        frame.getInputPanel().openFileButton.setVisible(visible);
        frame.getInputPanel().torqueEquation.setVisible(!visible);
        frame.getInputPanel().torqueEquationParameters.setVisible(!visible);
        frame.getInputPanel().testPeriodLabel.setVisible(visible);
        frame.getInputPanel().testPeriodsSpinner.setVisible(visible);
        frame.getInputPanel().itemsLoadedLabel.setVisible(visible);
        frame.getContentPane().revalidate();
        frame.getContentPane().repaint();
    }

    /**
     * set user input visibility for mixed test commands
     * 
     * @param visible
     */
    public void mixedTestVisibility(boolean visible) {
        frame.getInputPanel().stopTime.setVisible(!visible);
        frame.getInputPanel().filename.setVisible(visible);
        frame.getInputPanel().openFileButton.setVisible(visible);
        frame.getInputPanel().torqueEquation.setVisible(visible);
        frame.getInputPanel().torqueEquationParameters.setVisible(visible);
        frame.getInputPanel().testPeriodLabel.setVisible(visible);
        frame.getInputPanel().testPeriodsSpinner.setVisible(visible);
        frame.getInputPanel().itemsLoadedLabel.setVisible(visible);
        frame.getContentPane().revalidate();
        frame.getContentPane().repaint();
    }

    /**
     * return user input target IP
     * 
     * @return String
     */
    public String getTargetIP() {
        System.err.println(frame.getInputPanel().targetIP.getText());
        return frame.getInputPanel().targetIP.getText();
    }

    /**
     * Triggers window popup
     * 
     * @param message
     */
    public void alert(String message) {

        if (!this.popupVisible) {
            frame.getInputPanel().userMessageAlert.setMessage(message);
            frame.getInputPanel().userMessageAlert.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
            this.dialog = frame.getInputPanel().userMessageAlert.createDialog("Alerta");
            this.dialog.setModal(false);
            this.dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            this.dialog.setVisible(true);
            this.popupVisible = true;

        }else
        {

            this.popupVisible = false;
        }

    }

    /**
     * Returns torque equation parameters
     * 
     * @return Map<String, TorqueEquationParameter>
     */
    public Map<String, TorqueEquationParameter> getTorqueEquationParameters() {
        return frame.getInputPanel().torqueEquationParameters.getParameterValues();
    }

    /**
     * Gets test type
     * 
     * @return testTypes
     */
    public testTypes getTestType() {

        testTypes selectedTest = (testTypes) frame.getInputPanel().torqueTestModeComboBox.getSelectedItem();
        return selectedTest;
    }

    /**
     * Creates torque vs time plot
     */
    private void plotTorqueTime() {

        TorqueTimeValues torqueTime = new TorqueTimeValues(getController().getTorqueTimeValues());
        mainDataset.get(commands.TORQUE.seriesName).getSeries(commands.TORQUE_COMMAND.seriesName).clear();
        for (int i = 0; i < torqueTime.length(); i++) {
            mainDataset.get(commands.TORQUE.seriesName).getSeries(commands.TORQUE_COMMAND.seriesName).add(
                    Float.valueOf(torqueTime.getTimestamp(i)),
                    Float.valueOf(torqueTime.getValue(i)));
            ;
        }

    }

    /**
     * Creates torque vs time plot
     */
    private void plotSpeedTime() {

        TorqueTimeValues speedTime = new TorqueTimeValues(getController().getSpeedTimeValues());
        selfSustainedDataset.get(commands.DUT_SPEED.seriesName).getSeries(commands.DUT_SPEED.seriesName).clear();
        for (int i = 0; i < speedTime.length(); i++) {
            selfSustainedDataset.get(commands.DUT_SPEED.seriesName).getSeries(commands.DUT_SPEED.seriesName).add(
                    Float.valueOf(speedTime.getTimestamp(i)),
                    Float.valueOf(speedTime.getValue(i)));
            ;
        }

    }

    /**
     * Stores chart data to CSV file
     * 
     * @param filename
     */
    private void storeDataSet(String filename) {

        java.util.List<String> csv = new ArrayList<>();
        int maxItemCount = 0;
        if (this.DUTModeSelected()) {
            mainDataset.putAll(selfSustainedDataset);
        }
        for (String series : mainDataset.keySet()) {
            int itemCount = this.mainDataset.get(series).getSeries(series).getItemCount();
            System.out.print(series);
            System.out.print(" : ");
            System.out.print(itemCount);
            if (maxItemCount < itemCount) {
                maxItemCount = itemCount;
            }
        }
        String header = "";
        String aux = "";
        for (int j = 0; j < maxItemCount; j++) {
            for (String key : this.mainDataset.keySet()) {
                List<XYSeries> seriesList = this.mainDataset.get(key).getSeries();
                Iterator<XYSeries> it = seriesList.iterator();
                while (it.hasNext()) {
                    XYSeries currentSeries = it.next();

                    try {
                        Number x = currentSeries.getX(j);
                        Number y = currentSeries.getY(j);
                        aux += String.format("%s,", x);
                        aux += String.format("%s,", y);
                    } catch (IndexOutOfBoundsException e) {
                        aux += String.format("%s,", "");
                        aux += String.format("%s,", "");
                    }
                    header += "Tiempo [ms],";
                    header += String.format("%s,", currentSeries.getKey());

                }

            }
            if (j == 0) {

                csv.add(header);
            }
            csv.add(aux);
            aux = "";

        }
        File savefile = new File(filename + ".csv");
        savefile.getParentFile().mkdirs();
        try (
                BufferedWriter writer = new BufferedWriter(new FileWriter(filename +
                        ".csv"));) {
            for (String line : csv) {
                writer.append(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write dataset", e);
        }
        System.out.println("exporte como csv");
    }

    @SuppressWarnings("unused")
    public void validateInput() throws IllegalArgumentException {
        Float value;
        testTypes selectedTest = this.getTestType();
        String endtime_ms = "a";
        if (selectedTest == testTypes.TORQUE_VS_SPEED) {

            endtime_ms = frame.getInputPanel().stopTime.getText();

            System.out.println(endtime_ms);
        } else {
            TorqueTimeValues torquePath = getController().getTorqueTimeValues();
            endtime_ms = torquePath.getTimestamp(torquePath.length() - 1);
            if (torquePath.length() == 0) {
                throw new IllegalArgumentException(NO_FILE_SELECTED_MSG);
            } else if (torquePath.getMax() > MAX_TORQUE) {
                throw new IllegalArgumentException(FILE_EXCEEDS_MAX_TORQUE_MSG);
            } else if (torquePath.getMinTimeDelta() < MIN_COMMAND_DELTA) {
                throw new IllegalArgumentException(
                        FILE_EXCEEDS_MIN_COMMAND_DELTA_MSG);
            }
        }
        try {
            Integer endtime_int = Float.valueOf(endtime_ms).intValue();
        } catch (NumberFormatException e) {
            System.out.println(endtime_ms);
            if (selectedTest == testTypes.TORQUE_VS_SPEED) {
                throw new IllegalArgumentException(UNKNOWN_ENDTIME);
            } else {
                throw new IllegalArgumentException(UNKNOWN_FILE_FORMAT_MSG);
            }
        }

        if (selectedTest == testTypes.TORQUE_VS_SPEED || selectedTest == testTypes.MIXED_TEST) {
            Map<String, TorqueEquationParameter> parameters = frame.getInputPanel().torqueEquationParameters
                    .getParameterValues();
            try {
                value = Float.valueOf(parameters.get("A").getValue());
                value = Float.valueOf(parameters.get("B").getValue());
                value = Float.valueOf(parameters.get("C").getValue());
                value = Float.valueOf(parameters.get("D").getValue());
            } catch (Exception e) {
                throw new IllegalArgumentException(UNKNOWN_PARAMETER_FORMAT_MSG);
            }
            if (Float.valueOf(parameters.get("A").getValue()) > MAX_TORQUE) {

                throw new IllegalArgumentException(LARGE_BIAS_MSG);
            }
            if (Float.valueOf(parameters.get("D").getValue()) < 0) {
                throw new IllegalArgumentException(NEGATIVE_INERTIA_MSG);
            } else if (Float.valueOf(parameters.get("D").getValue()) > 0.2) {
                throw new IllegalArgumentException(LARGE_INERTIA_MSG);
            }
        }
        if (frame.getInputPanel().selfSustainedTestSelection.isSelected()) {
            TorqueTimeValues speedPath = getController().getSpeedTimeValues();
            if (speedPath.length() == 0) {
                throw new IllegalArgumentException(SELF_SUSTAINED_NO_FILE_SELECTED_MSG);
            } else if (speedPath.getMax() > MAX_SPEED) {
                throw new IllegalArgumentException(SELF_SUSTAINED_FILE_EXCEEDS_MAX_SPEED_MSG);
            } else if (speedPath.getMinTimeDelta() < MIN_COMMAND_DELTA) {
                throw new IllegalArgumentException(
                        SELF_SUSTAINED_FILE_EXCEEDS_MIN_COMMAND_DELTA_MSG);
            }
        }

    }

    public void handleStartCommand() {
        System.err.println("estoy en iniciar");
        frame.inputPanel.measurementsPanel.setVisible(true);
        getController().startMeasurements();
        try {
            Thread.sleep(400);
        } catch (Exception e) {
        }
        plotUpdater.execute();
        frame.getInputPanel().startButton.setText(PAUSE_BUTTON_LABEL);

        try {
            getController().start();
            blockInput(testStates.TEST_RUNNING);
        } catch (Exception e) {
            System.err.println("Tire error");
            blockInput(testStates.TEST_PARAMETER_READY);
            System.err.println(e.getMessage());
            alert(e.getMessage());
        }

    }

    public boolean popupIsVisible() {
        return this.popupVisible;
    }

    /**
     * Implements behaviour for button input
     */
    private class ButtonHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            String cmd = event.getActionCommand();
            System.err.println(cmd);
            System.err.println("aca");

            if (SET_TEST_PARAMETERS_BUTTON_LABEL.equals(cmd)) {
                handleTestParameterLoad();
            } else if (WRITE_CSV.equals(cmd)) {
                this.handleFileOutput();
            } else if (BROWSE_FILE_BUTTON_LABEL.equals(cmd)) {
                this.handleTorqueTimeFileInput();
            } else if (CONNECT_BUTTON_LABEL.equals(cmd)) {
                this.handleConnectCommand();
            } else if (SHUTDOWN_BUTTON_LABEL.equals(cmd)) {
                this.handleShutdownCommand();
            } else if (EMERGENCY_STOP_BUTTON_LABEL.equals(cmd)) {
                this.handleEmergencyCommand();
            } else if (POWER_ON_BUTTON_LABEL.equals(cmd)) {
                this.handlePowerOnCommand();
            } else if (START_BUTTON_LABEL.equals(cmd)) {
                handleStartCommand();
            } else if (PAUSE_BUTTON_LABEL.equals(cmd)) {
                this.handlePauseCommand();
                // TODO Agregar lógica de reinicio de ensayo
            } else if (RESUME_BUTTON_LABEL.equals(cmd)) {
                this.handleResumeCommand();
            } else if (SELF_SUSTAINED_TEST_LABEL.equals(cmd)) {
                this.handleSelfSustainedSelection();
            } else if (SELF_SUSTAINED_TEST_IMPORT_LABEL.equals(cmd)) {
                this.handleSelfSustainedFileImport();
            }
        }

        private void handleTestParameterLoad() {
            boolean testLoadSuccess = true;
            testTypes test = getTestType();
            try {
                validateInput();
            } catch (Exception e) {
                testLoadSuccess = false;
                alert(e.getMessage());
            }
            if (test == testTypes.TORQUE_VS_SPEED && testLoadSuccess) {
                try {
                    getController().setTestEndTime(frame.getInputPanel().stopTime.getText());
                    getController().selectTorqueVsSpeed();
                    getController().setTorqueVsSpeedParameters(
                            frame.getInputPanel().torqueEquationParameters.getParameterValues());

                    blockInput(testStates.TEST_PARAMETER_READY);

                } catch (ConnectException e) {
                    System.out.println(e.getStackTrace());
                    alert(e.getMessage());
                    testLoadSuccess = false;
                    blockInput(testStates.TEST_PARAMETER_LOAD);
                }
            } else if (test == testTypes.TORQUE_VS_TIME && testLoadSuccess) {
                // Esto hacerlo ultimo
                try {

                    getController().selectTorqueVsTime();
                    blockInput(testStates.TEST_PARAMETER_READY);
                } catch (ConnectException e) {

                    blockInput(testStates.TEST_PARAMETER_LOAD);
                }

            } else if (test == testTypes.MIXED_TEST && testLoadSuccess) {
                try {
                    getController().selectMixedTest();
                    getController().setTorqueVsSpeedParameters(
                            frame.getInputPanel().torqueEquationParameters.getParameterValues());
                    blockInput(testStates.TEST_PARAMETER_READY);

                } catch (Exception e) {
                    System.out.println(e.getStackTrace());
                    alert(e.getMessage());
                    blockInput(testStates.TEST_PARAMETER_LOAD);
                    testLoadSuccess = false;
                }

            }
            if (frame.getInputPanel().selfSustainedTestSelection.isSelected() && testLoadSuccess) {
                try {
                    getController().selectSelfSustainedMode();
                    blockInput(testStates.TEST_PARAMETER_READY);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    alert(e.getMessage());
                    testLoadSuccess = false;
                    blockInput(testStates.TEST_PARAMETER_LOAD);
                }
            }
            // blockInput(testStates.TEST_RUNNING);

            if (testLoadSuccess) {
                try {
                    Thread.sleep(150);
                    getController().executeQueuedCommands();
                    // TODO: ESTO TIENE QUE IR CUANDO DA EL OK DE ENSAYO CARGADO
                    alert(PRE_START_WARNING);
                    getController().setTestStatus(serverSideTestStatus.READY_TO_START);
                } catch (Exception e) {

                    alert(e.getMessage());
                    getController().dismissQueuedCommands();
                    blockInput(testStates.TEST_PARAMETER_LOAD);
                }
            }
        }

        private void handleSelfSustainedFileImport() {
            JFileChooser c = new JFileChooser();
            // Demonstrate "Open" dialog:
            int rVal = c.showOpenDialog(Views.this.frame);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                frame.getInputPanel().DUTFilename.setText(c.getSelectedFile().getName());
                String dir = c.getCurrentDirectory().toString();
                String path = dir + "\\" + frame.getInputPanel().DUTFilename.getText();

                getController().createSpeedTimeFromCSV(path);
                plotSpeedTime();
            }
            if (rVal == JFileChooser.CANCEL_OPTION) {
                frame.getInputPanel().filename.setText("");
            }
        }

        private void handleResumeCommand() {
            System.err.println("estoy en reanudar");
            frame.getInputPanel().startButton.setText(PAUSE_BUTTON_LABEL);

            try {
                getController().start();
                blockInput(testStates.TEST_RUNNING);
            } catch (Exception e) {
                System.err.println("Tire error");
                blockInput(testStates.TEST_PARAMETER_READY);
                System.err.println(e.getMessage());
                alert(e.getMessage());
            }

        }

        private void handleSelfSustainedSelection() {
            if (frame.getInputPanel().selfSustainedTestSelection.isSelected()) {
                alert(SELF_SUSTAINED_MODE_WARNING);
                selfSustaintedFrame.setVisible(true);
                frame.getInputPanel().DUTFilename.setVisible(true);
                frame.getInputPanel().openDUTFileButton.setVisible(true);
                createSelfSustainedChart();
            } else {
                selfSustaintedFrame.setVisible(false);
                frame.getInputPanel().DUTFilename.setVisible(false);
                frame.getInputPanel().openDUTFileButton.setVisible(false);

            }
        }

        private void handlePauseCommand() {
            frame.getInputPanel().startButton.setText(START_BUTTON_LABEL);
            try {
                getController().stop();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                alert(e.getMessage());
                frame.getInputPanel().startButton.setText(PAUSE_BUTTON_LABEL);

            }

        }

        private void handlePowerOnCommand() {
            System.err.println("estoy en potencia");

            try {

                getController().powerOn();
                blockInput(testStates.POWER_CONNECTED);

            } catch (Exception e) {
                System.err.println("Tire error");

                System.err.println(e.getMessage());
                alert(e.getMessage());
                blockInput(testStates.PLC_CONNECTED);
            }

        }

        private void handleEmergencyCommand() {
            System.err.println("estoy en EMG Stop");
            try {
                getController().emergencyStop();
            } catch (Exception e) {
                alert(e.getMessage());
            }
        }

        private void handleShutdownCommand() {
            getController().stopMeasurements();
            System.err.println("estoy en apagar");
            plotUpdater.cancel(true);
            try {

                getController().powerOff();
                getController().PLCStop();
                blockInput(testStates.TEST_END);
            } catch (Exception e) {
                System.err.println("Tire error");

                System.err.println(e.getMessage());
                alert(e.getMessage());
                // blockInput(testStates.TEST_RUNNING);
                blockInput(testStates.TEST_END);

            }

        }

        private void handleConnectCommand() {

            String url = getTargetIP();
            System.err.println(url);
            try {

                getController().connect(url);
                Thread.sleep(200);
                getController().PLCStart();
                blockInput(testStates.PLC_CONNECTED);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                alert(e.getMessage());

                // blockInput(testStates.PLC_CONNECTED);
                blockInput(testStates.INITIAL);
            }
        }

        private void handleTorqueTimeFileInput() {
            JFileChooser c = new JFileChooser();
            // Demonstrate "Open" dialog:
            int rVal = c.showOpenDialog(Views.this.frame);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                frame.getInputPanel().filename.setText(c.getSelectedFile().getName());
                String dir = c.getCurrentDirectory().toString();
                String path = dir + "\\" + frame.getInputPanel().filename.getText();

                getController().createTorqueTimeFromCSV(path);
                plotTorqueTime();
            }
            if (rVal == JFileChooser.CANCEL_OPTION) {
                frame.getInputPanel().filename.setText("");
            }
        }

        private void handleFileOutput() {
            String pattern = SAVEFILE_NAME_DATE_FORMAT;
            DateFormat df = new SimpleDateFormat(pattern);
            Date today = Calendar.getInstance().getTime();
            String todayAsString = df.format(today);
            storeDataSet(System.getenv("APPDATA") + CSV_FILEPATH + todayAsString);
        }
    }

    /**
     * Implements behaviour for
     * different test types
     * 
     */
    private class TestTypeHandler implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.getSource() == frame.getInputPanel().torqueTestModeComboBox) {

                if (frame.getInputPanel().torqueTestModeComboBox.getSelectedItem() == testTypes.TORQUE_VS_SPEED) {

                    torqueVsTimeVisibility(false);

                } else if (frame.getInputPanel().torqueTestModeComboBox
                        .getSelectedItem() == testTypes.TORQUE_VS_TIME) {

                    torqueVsTimeVisibility(true);
                } else if (frame.getInputPanel().torqueTestModeComboBox
                        .getSelectedItem() == testTypes.MIXED_TEST) {

                    mixedTestVisibility(true);
                }
            }

        }

    }
}
