package test;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class Monitor_ {
	/*
	@Test
	public void given_no_devices_should_produce_empty_datamart() {
		List<DeviceAccessor> devices = Collections.emptyList();
		Monitor monitor = new Monitor(devices);
		monitor.of(Configuration)
				.add(new DatamartCollector("configuration"))
				.run();
		File root = new File(Archetype.AssetDatamart,"configuration");
		assertThat(root.listFiles().length).isEqualTo(0);
	}

	@Test
	public void given_devices_should_produce_datamart() {
		List<DeviceAccessor> devices = asList(
				createDeviceAccessor("10.0.0.1", Model.of("x")),
				createDeviceAccessor("10.0.0.2", Model.of("DP2K-10S")),
				createDeviceAccessor("10.0.0.3", Model.of("DCP2000"))
		);
		Monitor monitor = new Monitor(devices);
		monitor.of(Configuration).run();

		File root = new File(Archetype.AssetDatamart,"configuration");
		assertThat(root.listFiles().length).isEqualTo(3);
		assertThat(new File(root,"10.0.0.1.inl").length()).isEqualTo(0);
		assertThat(new File(root,"10.0.0.2.inl").length()).isNotEqualTo(0);
		assertThat(new File(root,"10.0.0.3.inl").length()).isNotEqualTo(0);
	}

	@Test
	public void given_collectors_should_produce_datamarts_and_datalake() {
		List<DeviceAccessor> devices = asList(
				createDeviceAccessor("10.0.0.3", Model.of("DCP2000"))
		);
		Monitor monitor = new Monitor(devices);
		monitor.of(Configuration)
				.add(new DatamartCollector("dm_1"))
				.add(new DatamartCollector("dm_2"))
				.add(new DatalakeCollector("event"))
				.run();
		assertThat(new File(Archetype.AssetDatamart,"dm_1").listFiles().length).isEqualTo(1);
		assertThat(new File(Archetype.AssetDatamart,"dm_2").listFiles().length).isEqualTo(1);

	}

	private DeviceAccessor createDeviceAccessor(final String ip, final Model model) {
		return new DeviceAccessor() {
			private Device device = createDevice(ip);
			@Override
			public Device device() {
				return device;
			}


			@Override
			public DeviceAccessor init() {
				return this;
			}

			@Override
			public Model model() {
				return model;
			}

			@Override
			public String execute(Goal goal) {
				return model.books.stream()
						.map(g->g.formOf(goal))
						.map(f->new Query(f,""))
						.map(b->b.format(new HashMap<>()))
						.collect(joining("\n\n"));
			}
		};
	}

	private Device createDevice(String ip) {
		return new Device(ip);
	}

	 */
}
