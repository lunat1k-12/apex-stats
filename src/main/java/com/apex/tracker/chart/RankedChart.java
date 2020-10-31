package com.apex.tracker.chart;

import com.apex.tracker.entity.PlayerEntity;
import com.apex.tracker.entity.StatEntity;
import com.apex.tracker.notification.PlayersNotificator;
import com.apex.tracker.repository.PlayerRepository;
import com.apex.tracker.repository.StatRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankedChart {

    public static final int CHART_DAYS_INTERVAL = 7;
    private final StatRepository statRepository;
    private final PlayerRepository playerRepository;
    private final PlayersNotificator playersNotificator;

    @Scheduled(fixedDelay = 600_000)
    public void drawChart() throws IOException {

        ChartData playersData = loadDataSet();

        CategoryDataset dataset = DatasetUtils.createCategoryDataset(
                playersData.getPlayerLabels(), playersData.getDates(),
                playersData.getData());

        JFreeChart chart = ChartFactory.createLineChart("Ranked", "день", "очки ранкеда", dataset);
        var renderer = this.gerRenderer();
        this.configurePlot(chart, renderer);

        chart.setBackgroundPaint(new Color(48, 48, 48));
        chart.setBorderPaint(Color.BLUE);
        chart.getLegend().setBackgroundPaint(new Color(48, 48, 48));
        chart.getLegend().setItemFont(new Font("Serif", Font.PLAIN, 20));

        chart.setTitle(new TextTitle("Прогресс ранкеда за неделю",
                        new Font("Serif", Font.PLAIN, 25)
                )
        );
        chart.getTitle().setPaint(Color.WHITE);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(stream, chart, 840, 420);
        stream.flush();
//        playersNotificator.rankStatImage(stream);
        ChartUtils.saveChartAsPNG(new File("check.png"), chart, 840, 420);
        log.info("Draw");
    }

    private void configurePlot(JFreeChart chart, LineAndShapeRenderer renderer) {
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(new Color(48, 48, 48));
        plot.setRangeGridlinesVisible(false);
        plot.setDomainGridlinesVisible(false);

        plot.getDomainAxis().setAxisLinePaint(Color.white);
        plot.getRangeAxis().setAxisLinePaint(Color.white);

        plot.getDomainAxis().setLabelPaint(Color.white);
        plot.getDomainAxis().setTickLabelPaint(Color.white);
        plot.getDomainAxis().setLabelFont(new Font("Serif", Font.PLAIN, 20));

        plot.getRangeAxis().setLabelPaint(Color.white);
        plot.getRangeAxis().setTickLabelPaint(Color.white);
        plot.getRangeAxis().setLabelFont(new Font("Serif", Font.PLAIN, 20));
    }

    private LineAndShapeRenderer gerRenderer() {
        var renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(198, 29, 29));
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesPaint(1, new Color(50, 40, 182));
        renderer.setSeriesStroke(1, new BasicStroke(3.0f));
        renderer.setSeriesPaint(2, new Color(37, 161, 44));
        renderer.setSeriesStroke(2, new BasicStroke(3.0f));
        renderer.setSeriesPaint(3, new Color(227, 172, 28));
        renderer.setSeriesStroke(3, new BasicStroke(3.0f));
        renderer.setDefaultLegendTextPaint(Color.white);

        return renderer;
    }

    private ChartData loadDataSet() {
        List<PlayerEntity> players = playerRepository.findAll();
        String[] playerLabels = new String[players.size()];

        double[][] res = new double[players.size()][];

        for (int i=0; i<res.length; i++) {
            res[i] = loadPlayerDataSet(players.get(i));
            playerLabels[i] = players.get(i).getName();
        }

        List<String> listLabels = getDaysLabels();
        String[] labels = new String[listLabels.size()];
        for (int i=0; i<listLabels.size(); i++) {
            labels[i] = listLabels.get(i);
        }

        return ChartData.builder()
                .data(res)
                .dates(labels)
                .playerLabels(playerLabels)
                .build();
    }

    private double[] loadPlayerDataSet(PlayerEntity player) {
        LocalDateTime weekAgo = LocalDateTime.now().minus(1, ChronoUnit.WEEKS);
        List<StatEntity> stats = statRepository.findAllByUserNameAndDate(player.getName(), weekAgo);

        if (CollectionUtils.isEmpty(stats)) {
            double value = statRepository.findLastByName(player.getName())
                    .map(StatEntity::getRankScore)
                    .map(Long::doubleValue)
                    .orElse(0d);

            return getOneValueDataset(value);
        }

        double[] res = new double[CHART_DAYS_INTERVAL];
        int index = 0;

        while (!LocalDate.now().equals(weekAgo.toLocalDate())) {
            double value = getValue(stats, weekAgo.toLocalDate());
            res[index] = value;
            weekAgo = weekAgo.plus(1, ChronoUnit.DAYS);
            index++;
        }

        return res;
    }

    private Double getValue(List<StatEntity> stats, LocalDate borderDate) {
        return stats.stream()
                .filter(s -> s.getCreated().toLocalDate().equals(borderDate))
                .map(StatEntity::getRankScore)
                .mapToDouble(Long::doubleValue)
                .max()
                .orElseGet(() -> getPreviousValue(borderDate, stats));
    }

    private double getPreviousValue(LocalDate date, List<StatEntity> stats) {
        LocalDate prevDate =  stats.stream()
                .filter(s -> s.getCreated().toLocalDate().isBefore(date))
                .max(Comparator.comparing(StatEntity::getCreated))
                .map(StatEntity::getCreated)
                .map(LocalDateTime::toLocalDate)
                .orElse(date);

        return stats.stream()
                .filter(s -> s.getCreated().toLocalDate().equals(prevDate))
                .mapToDouble(StatEntity::getRankScore)
                .max()
                .orElse(0);
    }

    private List<String> getDaysLabels() {
        LocalDateTime weekAgo = LocalDateTime.now().minus(1, ChronoUnit.WEEKS);
        List<String> result = new ArrayList<>();
        var formatter = DateTimeFormatter.ofPattern("dd-MM");

        while (!LocalDate.now().equals(weekAgo.toLocalDate())) {
            result.add(formatter.format(weekAgo));
            weekAgo = weekAgo.plus(1, ChronoUnit.DAYS);
        }

        return result;
    }

    private double[] getOneValueDataset(Double value) {
        double[] res = new double[CHART_DAYS_INTERVAL];
        Arrays.fill(res, value);
        return res;
    }

    @Data
    @Builder
    private static class ChartData {
        private double[][] data;
        private String[] dates;
        private String[] playerLabels;
    }
}
