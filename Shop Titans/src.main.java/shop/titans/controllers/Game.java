package shop.titans.controllers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
		reagents.values().stream().forEach(r -> printItemsForComponent(r, 1));
	}
	
	private static void printItemsForComponent(Reagent r, int limit) {
		items.values().stream().filter(i -> {
			return i.getReagents().stream().anyMatch(ic -> {
				if (ic instanceof CraftReagentComponent ir) {
					return ir.getReagent().equals(r);
				}
				return false;
			});
		}).sorted().limit(limit)
		.forEach(i -> {
			log.info(i.getItemType().getName() + " " + i.getName() + "(id=" + i.getId() + ") is a best craft using " + r.getName() + "(Reagent (id=" + r.getId() + ") price: " + r.getPrice() + ". Profit " + i.getProfit() + "). Is final price? " + (r.isPriceCalculated() ? "Yes" : "Not"));
		});
	}
	
	private static void calculate() {
		var collector = items.values().stream().map(ItemNode::new).collect(Collectors.toCollection(TreeSet::new));
		collector = removeCalculatedComponents(collector);
		removeLowestComponents(collector);
	}
	
	private static void removeLowestComponents(TreeSet<ItemNode> collector) {
		var copy = collector;
		for (var reagent : reagents.values()) {
			if (reagent.isPriceCalculated()) {
				continue;
			}
			reagent.setCalculated();
			copy = removeCalculatedComponents(copy);
		}
	}

	private static TreeSet<ItemNode> removeCalculatedComponents(TreeSet<ItemNode> collect) {
		boolean containsOneComponent = true;
		var copy = collect; 
		while (containsOneComponent) {
			var temp = new TreeSet<ItemNode>();
			var components = new HashMap<Reagent, Integer>();
			containsOneComponent = false;
			for (ItemNode node : copy) {
				var size = node.leftedReagents.size();
				if (size == 0) {
					continue;
				}
				if (size == 1) {
					containsOneComponent = true;
					var reagent = node.leftedReagents.first();
					var component = reagent.getReagent();
					if (components.containsKey(component)) {
						components.computeIfPresent(component, (k, v) -> v=v+1);
					} else {
						components.put(component, 1);
					}
					if (component.isPriceCalculated()) {
						continue;
					}
					component.updatePrice((int) Math.ceil(Double.valueOf(node.leftedPrice) / reagent.getCount()));
					continue;
				}
				if (node.leftedPrice <= node.getCost()) {
					continue;
				}
				var leftedComponents = new TreeSet<CraftReagentComponent>();
				for (CraftReagentComponent component : node.leftedReagents) {
					if (component.getReagent().isPriceCalculated()) {
						node.leftedPrice = node.leftedPrice - component.getPrice();
						continue;
					}
					leftedComponents.add(component);
					var reagent = component.getReagent();
					if (components.containsKey(reagent)) {
						components.computeIfPresent(reagent, (k, v) -> v=v+1);
					} else {
						components.put(reagent, 1);
					}
				}
				node.leftedReagents = leftedComponents;
				temp.add(node);
			}
			reagents.forEach((k, v) -> {
				if (v.isPriceCalculated()) {
					return;
				}
				if (components.size() == 0) {
					return;
				}
				if (!components.containsKey(v)) {
					v.setCalculated();
				}
			});
			copy = temp;
		}
		return copy;
	}

	public static void printBestCraftForComponent(int id) {
		calculate();
		printItemsForComponent(getReagent(id), 100);		
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
		calculate();
		var id = getReagent(reagentName);
		if (id == NOT_FOUND) {
			log.error("Wrong reagent name");
			return;
		}
		printItemsForComponent(getReagent(id), 100);
	}

	private static Integer getReagent(String reagentName) {
		return reagents.values().stream().filter(e -> e.getName().equals(reagentName)).map(e -> e.getId()).findFirst().orElse(NOT_FOUND);
	}
}
