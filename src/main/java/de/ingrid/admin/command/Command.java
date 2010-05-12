package de.ingrid.admin.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to add and to execute registrated 
 *
 */
public class Command {
	private List<Command> commandList;
	
	public Command() {
		this.commandList = new ArrayList<Command>();
	}

	/**
	 * Add new command to a list of command.
	 * 
	 * @param command
	 */
	public void add(Command command){
		if(commandList == null){
			commandList = new ArrayList<Command>();
		}
		commandList.add(command);
	}
	
	/**
	 * Execution of all command in a command list.
	 */
	public void execute(){
		for(int i=0; i<commandList.size();i++){
			commandList.get(i).execute();	
		}
	}
	
	public void clear(){
		commandList.clear();
	}
	
}
