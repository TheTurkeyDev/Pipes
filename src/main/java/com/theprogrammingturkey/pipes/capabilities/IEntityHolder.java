package com.theprogrammingturkey.pipes.capabilities;

import java.util.List;

import net.minecraft.entity.Entity;

public interface IEntityHolder
{
	/**
	 * @author MJRLegends
	 * 
	 *         Returns a list of entities that were sucked up, or would have been sucked up
	 * @param simulate
	 *            determines whether or not the entities actually get sucked up
	 * @return List of entities sucked up
	 */
	public List<Entity> suckupEntities(boolean simulate);

	/**
	 * Places the given entities.
	 * 
	 * @param ent
	 *            List of entities to place
	 * @param simulate
	 *            determine whether or not to actually place the entities
	 * @return List of entities that were successfully placed
	 */
	public List<Entity> shootoutEntities(List<Entity> ents, boolean simulate);
}
