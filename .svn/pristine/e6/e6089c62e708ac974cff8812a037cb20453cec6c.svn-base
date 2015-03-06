package se.lth.cs.palcom.browsergui;

import ist.palcom.resource.descriptor.DeviceID;
import ist.palcom.resource.descriptor.PRDData;

import java.net.SocketException;

import se.lth.cs.palcom.device.AbstractDevice;
import se.lth.cs.palcom.util.device.network.NetworkSelector;

/**
 * The main device for the BrowserGUI, used by BrowserGUI to access the palcom
 * network
 * @author mattias
 */
public class MiniBrowserDevice extends AbstractDevice {

    private BrowserService mainService;
    private NetworkSelector network;

    public MiniBrowserDevice(BrowserApplication ui, DeviceID id, String name, String[] autoAddInterfaces) throws SocketException {
        super(id, name, "v0.01");

        network = new NetworkSelector(this);
        mainService = new BrowserService(this);
    }

    @Override
    public boolean start() {
    	if (!usesWP4()) network.startWorker();
        mainService.start();
        super.start();
        setStatus(PRDData.FULLY_OPERATIONAL);
        return true;
    }

    @Override
    public boolean stop() {
        mainService.stop();
        if (!usesWP4()) network.stopWorker();
        try {
            super.stop();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    public BrowserService getBrowserService() {
    	return mainService;
    }

	public NetworkSelector getNetwork() {
		return network;
	}
}
