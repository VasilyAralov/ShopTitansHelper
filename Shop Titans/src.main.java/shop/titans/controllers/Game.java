package shop.titans.controllers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import shop.titans.interfaces.ADBObject;
import shop.titans.interfaces.ICraftReagent;
import shop.titans.models.BossMapper;
import shop.titans.models.CraftComponentMapper;
import shop.titans.models.CraftItemMapper;
import shop.titans.models.ItemMapper;
import shop.titans.models.ItemTypeMapper;
import shop.titans.models.ReagentsMapper;
import shop.titans.views.Boss;
import shop.titans.views.CraftReagentComponent;
import shop.titans.views.CraftReagentItem;
import shop.titans.views.Item;
import shop.titans.views.Reagent;
import shop.titans.views.Type;

@Component
public class Game {
	
	private static final int NOT_FOUND = -1;
	private static JdbcTemplate jdbc;
	
	@Autowired
	Game(JdbcTemplate jdbc, Environment env) {
		Game.jdbc = jdbc;
		Game.maxStack = Integer.parseInt(env.getProperty("maxComponent"));
		Game.playerMulticraft = Integer.parseInt(env.getProperty("multicraftChance"));
	}
	
	private static Logger log = LoggerFactory.getLogger(Game.class);
	private static Game mainGame;

	private Game() {
		collect(bosses, BossMapper.getAllRecords(jdbc));
		collect(reagents, ReagentsMapper.getAllReagents(jdbc));
		collect(types, ItemTypeMapper.getAllTypes(jdbc));
		collect(items, ItemMapper.getAllItems(jdbc));
		CraftComponentMapper.getAllRecords(jdbc);
		CraftItemMapper.getAllRecords(jdbc);
	}

	private <T extends ADBObject> void collect(Map<Integer, T> map, Collection<T> list) {
		list.forEach(e -> {
			if (map.putIfAbsent(e.getId(), e) == null) {
				map.computeIfPresent(e.getId(), (k, v) -> v.merge(e));
			}
		});
	}

	public static void init() {
		if (Game.mainGame == null) {
			mainGame = new Game();
		}
	}
	
	private static Map<Integer, Boss> bosses = new TreeMap<>();
	private static Map<Integer, Reagent> reagents = new TreeMap<>();
	private static Map<Integer, Type> types = new TreeMap<>();
	private static Map<Integer, Item> items = new TreeMap<>();
	private static int maxStack = Integer.MAX_VALUE;
	private static int playerMulticraft = 0;

	public static Boss getBoss(int id) {
		try {
			return getGameObjectById(bosses, id, Boss.class.getConstructor(Integer.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Constructor with id parameter isn't implemented");
		}
	}
	
	private static <T extends ADBObject> T getGameObjectById(Map<Integer, T> objects, int id, Constructor<T> constructor) {
		var type = objects.get(id);
		if (type == null) {
			try {
				objects.put(id, constructor.newInstance(id));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new IllegalArgumentException("Wrong constructor");
			}
		}
		return objects.get(id);
	}

	public static void printBossSchedule(Boss... boss) {
		bosses.values().stream().sorted().forEach(e-> {
			log.info(e.toString() + ". Components: " + reagents.values().stream().filter(e1 -> {
				return e1.getBoss().equals(e);
			}).map(e1 -> e1.getName() + "=" + (maxStack - e1.getMaxBossDrop())).collect(Collectors.joining(",")));
		});
	}
	
	public static void printBossLoot() {
		reagents.values().stream().map(Reagent::toString).forEach(log::info);;
	}

	public static Type getItemType(int id) {
		try {
			return getGameObjectById(types, id, Type.class.getConstructor(Integer.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Constructor with id parameter isn't implemented");
		}
	}

	public static void printItems() {
		items.values().stream().map(Item::toString).forEach(log::info);
	}

	public static void printBestCraftPerComponent() {
		calculate();
		reagents.values().stream().forEach(r -> printItemsForComponent(r));
	}
	
	private static class ItemNode implements Comparable<ItemNode> {
		private Item item;
		private TreeSet<CraftReagentComponent> leftedReagents = new TreeSet<>();
		private int leftedPrice;
		
		private ItemNode(Item e) {
			this.item = e;
			this.leftedPrice = e.getPrice();
			e.getReagents().stream().forEach(e1 -> {
				if (e1 instanceof CraftReagentComponent comp) {
					leftedReagents.add(comp);
				}
				if (e1 instanceof CraftReagentItem item) {
					leftedPrice = leftedPrice - item.getPrice();
				}
			});
		}
		
		private int getCost() {
			var compPrice = leftedReagents.stream().mapToInt(ICraftReagent::getPrice).sum();
			return compPrice;
		}

		@Override
		public int compareTo(ItemNode that) {
			var priceDiff = (that.leftedPrice - that.getCost()) - (this.leftedPrice - this.getCost());
			if (priceDiff == 0) {
				priceDiff = this.item.getId() - that.item.getId();
			}
			return priceDiff;
		}
	}
	
	private static void calculate() {
		var itemsToCalculate = items.values().stream().map(ItemNode::new).collect(Collectors.toSet());
		int prevStep;
		do {
			do {
				prevStep = itemsToCalculate.size();
				itemsToCalculate = filter(itemsToCalculate);
			} while (itemsToCalculate.size() != prevStep);
			removeReagents(itemsToCalculate);
			itemsToCalculate.forEach(e -> log.error(e.item.toString()));
			log.error("----------------------------------------------------------------");
		} while (itemsToCalculate.size() > 0);
		if (itemsToCalculate.size() != 0) {

		}
	}

	private static Set<ItemNode> filter(Set<ItemNode> items) {
		return items.stream().filter(e -> {
			if (e.leftedReagents.size() == 0) {
				return false;
			}
			if (e.leftedReagents.size() == 1) {
				var reagent = e.leftedReagents.first();
				if (reagent.getReagent().isPriceCalculated()) {
					return false;
				}
				reagent.getReagent().updatePrice(e.leftedPrice / reagent.getCount());
				return false;
			}
			if (e.leftedPrice < e.getCost()) {
				return false;
			}
			e.leftedReagents = e.leftedReagents.stream().filter(el -> {
				if (el.getReagent().isPriceCalculated()) {
					e.leftedPrice -= el.getPrice();
					return false;
				}
				return true;
			}).collect(Collectors.toCollection(TreeSet::new));
			return true;
		}).collect(Collectors.toSet());
	}
	
	private static void removeReagents(Set<ItemNode> items) {
		log.error("----------------------------------------------------------------");
		Map<Reagent, Integer> reagentEntry = new TreeMap<>();
		for (var node : items) {
			for (var component : node.leftedReagents) {
				var counter = reagentEntry.getOrDefault(component.getReagent(), 0);
				reagentEntry.put(component.getReagent(), ++counter);
			}
		}
		reagentEntry.forEach((k, v) -> {
			log.error(k + " -> " + v);
			if (v == 1) {
				k.setCalculated();
			}
		});
	}
	
	private static void printItemsForComponent(Reagent r) {
		log.info(r.getName() + " (" + r.getPrice() +  "):");
		var reagentItems = items.values().stream().filter(i -> {
			return i.getReagents().stream().anyMatch(ic -> {
				if (ic instanceof CraftReagentComponent ir) {
					return ir.getReagent().equals(r);
				}
				return false;
			});
		}).sorted().collect(Collectors.toCollection(TreeSet<Item>::new));
		var first = reagentItems.first();
		for (Item item : reagentItems) {
			if (item.getProfit() + 3 < first.getProfit()) {
				break;
			}
			log.info(item.toString());
		}
	}
	
	
	
	public static void printBestCraftForComponent(int id) {
		printItemsForComponent(getReagent(id));		
	}

	public static Item getItem(int id) {
		try {
			return getGameObjectById(items, id, Item.class.getConstructor(Integer.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Constructor with id parameter isn't implemented");
		}
	}

	public static Reagent getReagent(int id) {
		try {
			return getGameObjectById(reagents, id, Reagent.class.getConstructor(Integer.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Constructor with id parameter isn't implemented");
		}
	}

	public static int getMulticraftChance() {
		return playerMulticraft;
	}

	public static void printBestCraftForComponent(String reagentName) {
		var id = getReagent(reagentName);
		if (id == NOT_FOUND) {
			log.error("Wrong reagent name");
			return;
		}
		printItemsForComponent(getReagent(id));
	}

	private static Integer getReagent(String reagentName) {
		return reagents.values().stream().filter(e -> e.getName().equals(reagentName)).map(e -> e.getId()).findFirst().orElse(NOT_FOUND);
	}
}
