package com.cinepolis.cosmos.monitor.goals.scrapper;

import com.cinepolis.cosmos.monitor.Logger;
import com.cinepolis.cosmos.monitor.goals.scrapper.scrappers.*;
import com.cinepolis.cosmos.monitor.goals.scrapper.scrappers.model.Cinema;
import com.cinepolis.cosmos.monitor.inventory.Division;
import com.cinepolis.cosmos.monitor.inventory.Theater;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.cinepolis.cosmos.monitor.Inventory.Divisions;
import static com.cinepolis.cosmos.monitor.goals.scrapper.ScreenWriter.*;
import static java.util.stream.Collectors.toList;

public class Website {
	private final List<Exhibition> exhibitions;

	public Website() {
		exhibitions = new ArrayList<>();
	}

	public void start() {
		Timer timer = new Timer();
		timer.schedule(updateTask(), 0, 6*Hours);
	}

	private static final int Hours = 1000 * 60  * 60;

	public synchronized void update() throws InterruptedException {
		Logger.info("Updating websites...");
		exhibitions.clear();
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(16);
		for (Division division: Divisions)
			executor.submit(() -> update(division));
		executor.awaitTermination(5, TimeUnit.MINUTES);
		Logger.info("Websites updated");
	}

	private void update(Division division)  {
		try {
			update(exhibitionsIn(division));
		} catch (InterruptedException ignored) {
		}
	}

	public List<Exhibition> exhibitionsIn(Division division) {
		return scrapperOf(division.country).exhibitionsIn(division);
	}

	private void update(List<Exhibition> exhibitions) throws InterruptedException {
		if (exhibitions.size() == 0) return;
		this.exhibitions.addAll(exhibitions);
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(16);
		for (Theater theater : theatersIn(exhibitions))
			executor.submit(() -> {
				Schedule schedule = scheduleOf(theater);
				for (Exhibition exhibition : exhibitions) {
					if (exhibition.theater != theater) continue;
					schedule.update(exhibition);
				}
			});
		executor.awaitTermination(5, TimeUnit.MINUTES);
	}

	@NotNull
	private List<Theater> theatersIn(List<Exhibition> exhibitions) {
		return exhibitions.stream().map(e -> e.theater).distinct().collect(toList());
	}

	private Schedule scheduleOf(Theater theater) {
		ScreenWriter screenWriter = new ScreenWriter(theater.segment + "1");
		if (screenWriter.isNotLogged()) Logger.error("It is not possible to log into " + theater);
		return screenWriter.schedule();
	}

	public List<Cinema> cinemasIn(List<Division> divisions) {
		return divisions.parallelStream()
				.map(division -> scrapperOf(division.country).cinemasIn(division))
				.flatMap(Collection::stream)
				.collect(toList());
	}

	public Exhibition update(Exhibition exhibition) {
		return scrapperOf(exhibition.country).update(exhibition);
	}

	public Stream<Exhibition> activeExhibitions() {
		Logger.info("Refreshing active exhibitions...");
		return query(minuteOfDay());
	}

	private Stream<Exhibition> query(int minuteOfDay) {
		return exhibitions.stream()
				.filter(Exhibition::hasTheater)
				.filter(exhibition -> exhibition.isActiveAt(minuteOfDay));
	}

	private final static int SecondsPerMinute = 60;

	public static int minuteOfDay() {
		return LocalTime.now().toSecondOfDay() / SecondsPerMinute;
	}
	private Scrapper scrapperOf(String country) {
		return Scrappers.getOrDefault(country, Scrapper.Null);
	}

	static Map<String, Scrapper> Scrappers = new HashMap<>() {{
		//put("Chile", new ChileScrapper());
		//put("Colombia", new ColombiaScrapper());
		//put("Costa Rica", new CostaRicaScrapper());
		//put("El Salvador", new SalvadorScrapper());
		put("Guatemala", new GuatemalaScrapper());
		//put("Honduras", new HondurasScrapper());
		//put("Mexico", new MexicoScrapper());
		//put("Panama", new PanamaScrapper());
		//put("Spain", new SpainScrapper());
	}};

	private TimerTask updateTask() {
		return new TimerTask() {
			@Override
			public void run() {
				try {
					update();
				} catch (InterruptedException ignored) {

				}
			}
		};
	}


}
