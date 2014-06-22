package me.cmesh.HopperFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HopperListener implements Listener {
	private void FilterHopper(Inventory inv, ItemStack item, Cancellable ev) {
		if (inv.getHolder() instanceof Hopper) {
			Hopper h = (Hopper)inv.getHolder();
			Location compare = h.getLocation().add(0.5,0.5,0.5);//Middle of block
			
			List<ItemStack> validItems = new ArrayList<ItemStack>();
			List<ItemStack> invalidItems = new ArrayList<ItemStack>();

			for (Entity e : h.getLocation().getWorld().getEntities()) {
				if (e instanceof ItemFrame) {
					if (e.getLocation().distance(compare) < 1) {
						ItemFrame frame = (ItemFrame)e;
						Block hop = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());
						if (hop.equals(h.getBlock())) {
							if (frame.getRotation() == Rotation.FLIPPED) {
								invalidItems.add(frame.getItem());
							} else {
								validItems.add(frame.getItem());
							}
						}
					}
				}
			}
			
			if (!validItems.isEmpty() && !validItems.contains(item)) {
				ev.setCancelled(true);
				return;
			}
			
			if (invalidItems.contains(item)) {
				ev.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void hopperGrabItem(InventoryPickupItemEvent ev) {
		ItemStack compare = ev.getItem().getItemStack().clone();
		compare.setAmount(1);
		FilterHopper(ev.getInventory(), compare, ev);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void hopperMoveItem(InventoryMoveItemEvent ev) {
		ItemStack compare = ev.getItem().clone();
		compare.setAmount(1);
		
		int maxitems = 7; 
		
		FilterHopper(ev.getDestination(), compare, ev);
		if (!ev.isCancelled() && ev.getItem().getMaxStackSize() != 1) { //not canceled and not a tool
			ItemStack item = ev.getItem().clone();
			
			
			List<InventoryType> f = new ArrayList<InventoryType>();
			f.add(InventoryType.CHEST);
			f.add(InventoryType.HOPPER);
			
			if (!f.contains(ev.getDestination().getType()) || !f.contains(ev.getSource().getType())) {
				return;
			}
			
			int loc = ev.getSource().first(item.getType());
			if (loc == -1) {
				return;
			}
			
			ItemStack atloc = ev.getSource().getItem(loc);
			int totake = Math.min(atloc.getAmount(), maxitems);
			
			item.setAmount(totake);
			HashMap<Integer, ItemStack> res = ev.getDestination().addItem(item);
			for (ItemStack leftover : res.values()) {
				if (leftover != null) {
					totake -= leftover.getAmount();
				}
			}
			
			atloc.setAmount(atloc.getAmount() - totake);
			ev.getSource().setItem(loc, atloc);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void playerMoveItem(org.bukkit.event.inventory.InventoryClickEvent ev) {
		if (ev.getCurrentItem() == null) {
			return;
		}
		
		ItemStack compare = ev.getCurrentItem().clone();
		compare.setAmount(1);
		FilterHopper(ev.getInventory(), compare, ev);
	}
}
