import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class XYLineChart_AWT {


    public static final HashMap<String, Path> robotPaths = new HashMap<String, Path>();
    public static final Trajectory.Config defaultConfig = new Trajectory.Config(
            Trajectory.FitMethod.HERMITE_QUINTIC, Trajectory.Config.SAMPLES_HIGH, 0.005, 145, 95, 200);
    public static final Trajectory.Config slowConfig = new Trajectory.Config(
            Trajectory.FitMethod.HERMITE_QUINTIC, Trajectory.Config.SAMPLES_HIGH, 0.005, 130, 85, 120);
    public static final Trajectory.Config slowerConfig = new Trajectory.Config(
            Trajectory.FitMethod.HERMITE_QUINTIC, Trajectory.Config.SAMPLES_HIGH, 0.005, 100, 60, 100);
    public static final double SWITCH_X = 135;

    static {
        robotPaths.put("CSR-1", new Path(new Waypoint[]{
                new Waypoint(23, 156, Pathfinder.d2r(0)),
                new Waypoint(127, 100+18, Pathfinder.d2r(0)),
        }, defaultConfig));

        robotPaths.put("CSR-2", new Path(new Waypoint[]{
                new Waypoint(130, 100+18, Pathfinder.d2r(0)),
                new Waypoint(102, 109+18, Pathfinder.d2r(-45)),
                new Waypoint(93, 131+18, Pathfinder.d2r(-90)),
                new Waypoint(101, 150+18, Pathfinder.d2r(-120)),
        }, defaultConfig
        ));

        robotPaths.put("CSR-3", new Path(new Waypoint[]{
                new Waypoint(101, 150+18, Pathfinder.d2r(-120)),
                new Waypoint(93, 131+18, Pathfinder.d2r(-90)),
                new Waypoint(102, 109+18, Pathfinder.d2r(-45)),
                new Waypoint(130, 100+18, Pathfinder.d2r(0)),
        }, defaultConfig));

        robotPaths.put("CSR-4", new Path(new Waypoint[]{
                new Waypoint(130, 100+18, Pathfinder.d2r(0)),
                new Waypoint(114, 106+18, Pathfinder.d2r(-45)),
                new Waypoint(107, 123+18, Pathfinder.d2r(-90)),
                new Waypoint(114, 142+18, Pathfinder.d2r(-120)),
        }, defaultConfig));

        robotPaths.put("CSR-5", new Path(new Waypoint[]{
                new Waypoint(114, 142+18, Pathfinder.d2r(-120)),
                new Waypoint(107, 123+18, Pathfinder.d2r(-90)),
                new Waypoint(114, 106+18, Pathfinder.d2r(-45)),
                new Waypoint(130, 100+18, Pathfinder.d2r(0)),
        }, defaultConfig));

        robotPaths.put("CSL-1", new Path(new Waypoint[]{
                new Waypoint(23, 156, Pathfinder.d2r(0)),
                new Waypoint(SWITCH_X, 218 - 12, Pathfinder.d2r(0)),
        }, defaultConfig));

        robotPaths.put("CSL-2", new Path(new Waypoint[]{
                new Waypoint(130, 215, Pathfinder.d2r(0)),
                new Waypoint(105, 207, Pathfinder.d2r(45)),
                new Waypoint(97, 187, Pathfinder.d2r(90)),
                new Waypoint(98, 181, Pathfinder.d2r(120)),
        }, defaultConfig));

        robotPaths.put("CSL-3", new Path(new Waypoint[]{
                new Waypoint(98, 181, Pathfinder.d2r(120)),
                new Waypoint(97, 187, Pathfinder.d2r(90)),
                new Waypoint(105, 207, Pathfinder.d2r(45)),
                new Waypoint(130, 215, Pathfinder.d2r(0)),
        }, defaultConfig));

        robotPaths.put("CSL-4", new Path(new Waypoint[]{
                new Waypoint(130, 215, Pathfinder.d2r(0)),
                new Waypoint(116, 209, Pathfinder.d2r(45)),
                new Waypoint(110, 194, Pathfinder.d2r(90)),
                new Waypoint(112, 184, Pathfinder.d2r(120)),
        }, slowConfig));

        robotPaths.put("CSL-5", new Path(new Waypoint[]{
                new Waypoint(112, 184, Pathfinder.d2r(120)),
                new Waypoint(110, 194, Pathfinder.d2r(90)),
                new Waypoint(116, 209, Pathfinder.d2r(45)),
                new Waypoint(130, 215, Pathfinder.d2r(0)),
        }, slowerConfig));

        robotPaths.put("DriveStraight", new Path(new Waypoint[]{
                new Waypoint(23, 156, 0),
                new Waypoint(SWITCH_X, 156, 0)
        }, defaultConfig));

    }

    public static void main(String[] args) {
        double dt = System.nanoTime() * 1e-9;
        double time = 0;
        HashMap<String, Trajectory> swerdPaths = new HashMap<>();
        for (Map.Entry<String, Path> path : robotPaths.entrySet()) {
            Trajectory trajectory = Pathfinder
                    .generate(path.getValue().getPoints(), path.getValue().config);
            File pathFile = new File("paths/" + path.getKey() + ".csv").getAbsoluteFile();
            Pathfinder.writeToCSV(pathFile, trajectory);
            System.out
                    .println("Path: " + path.getKey() + " Time: " + trajectory.length() * 0.005 + " Sec");
            time += trajectory.length() * 0.005;
            System.out.println((System.nanoTime() * 1e-9) - dt);
            swerdPaths.put(path.getKey(), trajectory);
        }
        System.out.println(time);

     /*   XYLineChart_AWT chart = new XYLineChart_AWT("FRC 1836 Robot Auto Path",
                "Robot Path", swerdPaths);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true); */

    }

	/*public XYLineChart_AWT(String applicationTitle, String chartTitle,
			HashMap<String, Trajectory> rPaths) {
		super(applicationTitle);

		final XYSeriesCollection dataset = new XYSeriesCollection();
		for (Map.Entry<String, Trajectory> path : rPaths.entrySet()) {
			final XYSeries graph = new XYSeries(path.getKey());
			for (Trajectory.Segment segment : path.getValue().segments) {
				graph.add(segment.x, segment.y);
			}

			dataset.addSeries(graph);
		}

		JFreeChart xylineChart = ChartFactory.createXYLineChart(
				chartTitle,
				"X",
				"Y",
				dataset,
				PlotOrientation.HORIZONTAL,
				true, false, false);

		ChartPanel chartPanel = new ChartPanel(xylineChart);
		chartPanel.setPreferredSize(new Dimension(560, 367));
		chartPanel.getChart().getXYPlot().getRangeAxis().setInverted(true);
		BufferedImage image = null;
		File url = new File("path_generator/field.png");
		System.out.println(url.getAbsolutePath());
		try {
			image = ImageIO.read(url);
		} catch (IOException e) {
			System.out.println(e);
		}
		xylineChart.setBackgroundImage(image);
		xylineChart.getPlot().setBackgroundAlpha(0);
		xylineChart.setBackgroundImageAlignment(100);

		final XYPlot plot = xylineChart.getXYPlot();

		NumberAxis domain = (NumberAxis) plot.getDomainAxis();
		domain.setRange(0, 150);
		domain.setTickUnit(new NumberTickUnit(50));
		domain.setVerticalTickLabels(true);
		NumberAxis range = (NumberAxis) plot.getRangeAxis();
		range.setRange(0.0, 324);
		range.setTickUnit(new NumberTickUnit(50));

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

		for (int i = 0; i < rPaths.size(); i++) {
			renderer.setSeriesPaint(i, Color.RED);
			renderer.setSeriesStroke(i, new BasicStroke(1.0f));
			renderer.setSeriesShapesVisible(i, false);
		}
		plot.setRenderer(renderer);
		setContentPane(chartPanel);
	} */

    static class Path {

        Waypoint[] points;
        Trajectory.Config config;

        public Path(Waypoint[] points, Trajectory.Config config) {
            this.points = points;
            this.config = config;
        }

        public Waypoint[] getPoints() {
            return points;
        }

        public Trajectory.Config getConfig() {
            return config;
        }

    }
}




