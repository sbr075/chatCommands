package me.Skimm.chatEmotes;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageHandler {	

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
		case 1: // sender
			sender.sendMessage(msg);
			break;
			
		case 2: // broadcast
			Bukkit.broadcastMessage(msg);
			break;
			
		case 3: // receiver
			receiver.sendMessage(msg);
			break;	
		}
    }
    
    public void msgParser(Player sender, String[] argv, ArrayList<ArrayList<String>> emoteAllInfo) {
    	int maxDistance, snumArgs, mnumArgs;
    	maxDistance = Integer.parseInt(emoteAllInfo.get(0).get(0));
    	snumArgs = Integer.parseInt(emoteAllInfo.get(1).get(0));
    	mnumArgs = Integer.parseInt(emoteAllInfo.get(2).get(0));

    	for (int i = 1; i < emoteAllInfo.size(); i++) {
    		if ((argv.length - 1 ) < Integer.parseInt(emoteAllInfo.get(i).get(0)))
    			continue;

    		for (int j = 1; j < emoteAllInfo.get(i).size(); j++) {
    			if (emoteAllInfo.get(i).get(j).equalsIgnoreCase("<BLANK>"))
    				continue;
    			
    			// Skips empty cases
    			switch(i) {
    			case 1:
    				if (snumArgs <= 0 || (argv.length - 1) != snumArgs)
    					continue;
    				break;
    			case 2:
    				if (mnumArgs <= 0 || (argv.length - 1) != mnumArgs)
    					continue;
    				break;
    			}

    			Player receiver = null;
    	    	if (argv.length >= 3) {
    	    		try {
    	    			receiver = sender.getServer().getPlayer(argv[2]);
    	    		}
    	    		catch (Exception e) {
    	    			sender.sendMessage("Emote " + argv[1] + " is set up wrong or receiver doesn't exist, use /e edit to fix it");
    	    		}
    	    	}
        		
    			String msg = "";
        		String[] tokens = emoteAllInfo.get(i).get(j).split(" ");

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
            	case 0:
            		msg = ChatColor.translateAlternateColorCodes('&', emoteAllInfo.get(i).get(j));
            		break;
            	
            	// sCount or rCount is 1 (single arg)
            	case 2:
            		if (thSplit.get(0).equalsIgnoreCase("")) { // start
            			switch(sCount) {
            			case 0: // r
            				msg = ChatColor.translateAlternateColorCodes('&', receiver.getDisplayName() + " " + thSplit.get(1));
            				break;
            				
            			case 1: // s
            				msg = ChatColor.translateAlternateColorCodes('&', sender.getDisplayName() + " " + thSplit.get(1));
            				break;
            			}
            		}
            		else if (thSplit.get(1).equalsIgnoreCase("")) { // end
            			switch(sCount) {
            			case 0:
            				msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + receiver.getDisplayName());
            				break;
            			case 1:
            				msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + sender.getDisplayName());
            				break;
            			}
            		}
            		else { // middle
            			switch(sCount) {
            			case 0:
            				msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + receiver.getDisplayName() + thSplit.get(1));
            				break;
            			}
            		}
            		break;
            	
            	// sCount and rCount is 1 
            	case 3:
            		// Check if distance is negative or zero
            		if (maxDistance <= 0) {
            			// If so, skip close
            			if (i >= 5 && i > 8) {
            				continue;
            			}
            		}
            		if (thSplit.get(0).equalsIgnoreCase("") && thSplit.get(2).equalsIgnoreCase("")) { // start end
            			switch(first) {
            			case 1: // s first
            				msg = ChatColor.translateAlternateColorCodes('&', sender.getDisplayName() + " " + thSplit.get(1) + "&f" + receiver.getDisplayName());
            				break;
            			case 2: // r first
            				msg = ChatColor.translateAlternateColorCodes('&', receiver.getDisplayName() + " " + thSplit.get(1) + "&f" +  sender.getDisplayName());
            				break;
            			}
            		}
            		else if (thSplit.get(0).equalsIgnoreCase("")) { // start middle
            			switch(first) {
        	    		case 1: // s first
        	    			msg = ChatColor.translateAlternateColorCodes('&', sender.getDisplayName() + " " + thSplit.get(1) + "&f" + receiver.getDisplayName()) + " " + thSplit.get(2);
        					break;
        				case 2: // r first
        					msg = ChatColor.translateAlternateColorCodes('&', receiver.getDisplayName() + " " + thSplit.get(1) + "&f" + sender.getDisplayName()) + " " + thSplit.get(2);
        					break;
        				}
            		}
            		else if (thSplit.get(2).equalsIgnoreCase("")) { // middle end 
            			switch(first) {
        	    		case 1: // s first
        	    			msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" +  sender.getDisplayName() + thSplit.get(1) + "&f" +  receiver.getDisplayName());
        					break;
        				case 2: // r first
        					msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" +  receiver.getDisplayName() + thSplit.get(1) + "&f" +  sender.getDisplayName());
        					break;
        				}
            		}
            		else { // middle middle
            			switch(first) {
        	    		case 1: // s first
        	    			msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + sender.getDisplayName() + " " + thSplit.get(1) + "&f" + receiver.getDisplayName() + " " + thSplit.get(2));
        					break;
        				case 2: // r first
        					msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + receiver.getDisplayName() + " " + thSplit.get(1) + "&f" + sender.getDisplayName() + " " + thSplit.get(2));
        					break;
        				}
            		}
            		
            		break;
            		
            	default:
            		sender.sendMessage(ChatColor.RED + "Something went wrong! Please report this to the mod author");
            	}

            	// Send message
            	msgSend(sender, msg, argv, j);
    		}
    	}
    }
}
