package clase;

import java.util.ArrayList;
import java.util.List;

public class NewsChannel {
	private String name;
	private String description;
	private Client channelOwner;
	private List<Client> subscribers = new ArrayList<Client>();
	
	public NewsChannel(String name, String description, Client channelOwner) {
		super();
		this.name = name;
		this.description = description;
		if(channelOwner != null)
			this.channelOwner = channelOwner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Client getChannelOwner() {
		return channelOwner;
	}

	public void setChannelOwner(Client channelOwner) {
		this.channelOwner = channelOwner;
	}

	public List<Client> getSubscribers() {
		return subscribers;
	}
	
	
	
	

}
