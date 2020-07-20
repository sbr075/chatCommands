package me.Skimm.chatEmotes;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.Skimm.chatCommands.Main;

public class MessageHandler {	
	private Main plugin;
	
	public MessageHandler (Main plugin) {
		this.plugin = plugin;
	}

	// Sends message out
	private void msgSend(Player sender, String msg, String[] argv, int msgType) {
    	Player receiver = null;
    	if (argv.length >= 3) {
    		try {
    			receiver = sender.getServer().getPlayer(argv[2]);
    		}
    		catch (Exception e) {
    			sender.sendMessage("Can't find player '" + argv[2] + "'");
    		}
    	}

    	switch(msgType) {
    	case 0: // receiver
    		receiver.sendMessage(msg);
			break;	
			
		case 1: // sender
			sender.sendMessage(msg);
			break;
			
		case 2: // broadcast
			Bukkit.broadcastMessage(msg);
			break;
		}
    }
	    
	/*
	 *  Parses through all emote information
	 *  0 - Max distance
	 *  1 - Single, Sender message
	 *  2 - Single, Broadcast message
	 *  3 - Single, fake receiver # NOT IN USE
	 *  4 - Multiple, close, sender
	 *  5 - Multiple, close, receiver
	 *  6 - Multiple, close, broadcast
	 *  7 - Multiple, far, sender
	 *  8 - Multiple, far, receiver
	 *  9 - Multiple, far, broadcast
	 */
    public void msgParser(Player sender, String[] argv, ArrayList<String> emoteAllInfo) {
    	int sBlank = 0, mcBlank = 0, mfBlank = 0;

    	for (int i = 1; i < emoteAllInfo.size(); i++) {    	
    		// Get receiver
			Player receiver = null;
	    	if (argv.length >= 3) {
	    		try {
	    			receiver = sender.getServer().getPlayer(argv[2]);
	    		}
	    		catch (Exception e) {
	    			sender.sendMessage("Emote " + argv[1] + " is set up wrong or receiver doesn't exist, use /emote edit to fix it");
	    		}
	    	}
	    	
	    	if (i < 4) {
	    		if (argv.length == 3)
	    			continue;
	    		
	    		if (emoteAllInfo.get(i).equalsIgnoreCase("<BLANK>")) {
    				sBlank++;
    				
    				if (sBlank == 3) {
    					sender.sendMessage(ChatColor.RED + "[WARNING]" + ChatColor.WHITE + " Emote '" + argv[1] + "' has no single target messages");
    					break;
    				}
    				continue;
    			}
	    	}
	    	else {
	    		if (argv.length == 2)
	    			break;
	    		
	    		if (i < 7) {
	    			if (Integer.parseInt(emoteAllInfo.get(0)) <= 0 || Integer.parseInt(emoteAllInfo.get(0)) < sender.getLocation().distance(receiver.getLocation()))
	        			continue;
	    			
	    			if (emoteAllInfo.get(i).equalsIgnoreCase("<BLANK>")) {
	    				mcBlank++;
	    				
	    				if (mcBlank == 3) {
	    	    			sender.sendMessage(ChatColor.RED + "[WARNING]" + ChatColor.WHITE + " Emote '" + argv[1] + "' has no close mutli target messages");
	    	    		}
	    				continue;
	    			}
	    		}
	    		else {
	    			if (Integer.parseInt(emoteAllInfo.get(0)) > sender.getLocation().distance(receiver.getLocation()))
	        			continue;
	    			
	    			if (emoteAllInfo.get(i).equalsIgnoreCase("<BLANK>")) {
	    				mfBlank++;
	    				
	    				if (mfBlank == 3) {
	    	    			sender.sendMessage(ChatColor.RED + "[WARNING]" + ChatColor.WHITE + " Emote '" + argv[1] + "' has no far multi target messages");
	    	    		}
	    				continue;
	    			}
	    		}
	    	}

	    	// Split up and parse message for keywords
			String msg = "";
    		String[] tokens = emoteAllInfo.get(i).split(" ");

        	ArrayList<String> thSplit = new ArrayList<String>();

        	int sCount, rCount, first;
        	sCount = rCount = first = 0;

        	String tmp = "";
        	for (String word : tokens) {
        		if (sCount > 1 || rCount > 1) {
        			sender.sendMessage(ChatColor.RED + "Message can't have more than one sender/receiver");
        			return;
        		}
        		
        		if (word.length() > 1) {
        			tmp += word + " ";
        			continue;
        		}
        		
        		if (word.equalsIgnoreCase("s")) {
        			thSplit.add(tmp);
        			
        			tmp = "";
        			sCount++;
        			
        			if (rCount == 0) {
        				first = 1;
        			}
        		}
        		
        		else if (word.equalsIgnoreCase("r")) {
        			thSplit.add(tmp);
        			
        			tmp = " ";
        			rCount++;
        			
        			if (sCount == 0) {
        				first = 2;
        			}
        		}
        	}

        	if (sCount != 0 || rCount != 0)
        		thSplit.add(tmp);
        	
        	// sCount and rCount is 0
        	switch(thSplit.size()) {
        	case 0: // No r or s
        		msg = ChatColor.translateAlternateColorCodes('&', emoteAllInfo.get(i));
        		break;
        	
        	// sCount or rCount is 1 (single arg)
        	case 2:
        		if (thSplit.get(0).equalsIgnoreCase("")) { // start
        			switch(sCount) {
        			case 0: // Only r
        				msg = ChatColor.translateAlternateColorCodes('&', plugin.stripName(receiver) + " " + thSplit.get(1));
        				break;
        				
        			case 1: // only s
        				msg = ChatColor.translateAlternateColorCodes('&', plugin.stripName(sender) + " " + thSplit.get(1));
        				break;
        			}
        		}
        		else if (thSplit.get(1).equalsIgnoreCase("")) { // end
        			switch(sCount) {
        			case 0: // only r
        				msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + plugin.stripName(receiver));
        				break;
        			case 1: // only s
        				msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + plugin.stripName(sender));
        				break;
        			}
        		}
        		else { // middle
        			switch(sCount) {
        			case 0:
        				msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + plugin.stripName(receiver) + " " + thSplit.get(1));
        				break;
        			case 1:
        				msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + plugin.stripName(sender) + " " + thSplit.get(1));
        				break;
        			}
        		}
        		break;
        	
        	// sCount and rCount is 1 
        	case 3:
        		if (thSplit.get(0).equalsIgnoreCase("") && thSplit.get(2).equalsIgnoreCase("")) { // start end
        			switch(first) {
        			case 1: // s first
        				msg = ChatColor.translateAlternateColorCodes('&', plugin.stripName(sender) + " " + thSplit.get(1) + "&f" + plugin.stripName(receiver));
        				break;
        			case 2: // r first
        				msg = ChatColor.translateAlternateColorCodes('&', plugin.stripName(receiver) + " " + thSplit.get(1) + "&f" +  plugin.stripName(sender));
        				break;
        			}
        		}
        		else if (thSplit.get(0).equalsIgnoreCase("")) { // start middle
        			switch(first) {
    	    		case 1: // s first
    	    			msg = ChatColor.translateAlternateColorCodes('&', plugin.stripName(sender) + " " + thSplit.get(1) + "&f" + plugin.stripName(receiver) + " " + thSplit.get(2));
    					break;
    				case 2: // r first
    					msg = ChatColor.translateAlternateColorCodes('&', plugin.stripName(receiver) + " " + thSplit.get(1) + "&f" + plugin.stripName(sender) + " " + thSplit.get(2));
    					break;
    				}
        		}
        		else if (thSplit.get(2).equalsIgnoreCase("")) { // middle end 
        			switch(first) {
    	    		case 1: // s first
    	    			msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" +  plugin.stripName(sender) + " " + thSplit.get(1) + "&f" + plugin.stripName(receiver));
    					break;
    				case 2: // r first
    					msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" +  plugin.stripName(receiver) + " " + thSplit.get(1) + "&f" +  plugin.stripName(sender));
    					break;
    				}
        		}
        		else { // middle middle
        			switch(first) {
    	    		case 1: // s first
    	    			msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + plugin.stripName(sender) + " " + thSplit.get(1) + "&f" + plugin.stripName(receiver) + " " + thSplit.get(2));
    					break;
    				case 2: // r first
    					msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + plugin.stripName(receiver) + " " + thSplit.get(1) + "&f" + plugin.stripName(sender) + " " + thSplit.get(2));
    					break;
    				}
        		}
        		
        		break;
        		
        	default:
        		sender.sendMessage(ChatColor.RED + "Something went wrong! Please report this to the mod author");
        	}

        	// Send message
        	msgSend(sender, msg, argv, i % 3);
    	}
    }
}
