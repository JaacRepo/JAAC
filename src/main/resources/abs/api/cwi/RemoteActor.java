package abs.api.cwi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public abstract class RemoteActor extends ActorSystem {

	private URI name;


	
	//static Socket s = null;

	public RemoteActor(URI name) {
		super();
		this.name = name;

		final String checkIP = this.name.getHost();

		if (checkIP.equals(s.getInetAddress().getHostAddress()) == false) {
			System.out.println(
					checkIP + "!=" + s.getInetAddress().getHostAddress());
		}

	}

	@Override
	public <V> Future<V> send(Object message) {
		Message m = new Message(message, IS_REACHABLE);
		if (machineOutputStreams != null
				&& machineOutputStreams.containsKey(name.getHost())) {
			ObjectOutputStream destination = machineOutputStreams
					.get(name.getHost());
			try {
				destination.writeObject(this.name);
				destination.writeObject(message);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				s = new Socket(this.name.getHost(), 1888);
				ObjectOutputStream oos = new ObjectOutputStream(
						s.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(
						s.getInputStream());

				if (machineOutputStreams == null) {
					machineOutputStreams = new ConcurrentHashMap<String, ObjectOutputStream>();
				}

				machineOutputStreams.put(s.getInetAddress().getHostAddress(),
						oos);
				oos.writeObject(this.name);
				oos.writeObject(message);

				if (machineInputStreams == null) {
					machineInputStreams = new ConcurrentHashMap<String, ObjectInputStream>();
				}

				machineInputStreams.put(s.getInetAddress().getHostAddress(),
						ois);

				new Thread(new MachineListener(ois)).start();

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return (Future<V>) m.f;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RemoteActor)
			return ((RemoteActor) obj).name.equals(name);
		else if (obj instanceof URI)
			return ((URI) obj).equals(name);
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
