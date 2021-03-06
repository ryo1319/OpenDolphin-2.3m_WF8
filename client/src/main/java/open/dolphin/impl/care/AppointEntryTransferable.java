package open.dolphin.impl.care;

import java.awt.datatransfer.*;
import java.io.IOException;
import open.dolphin.infomodel.AppointmentModel;
     
/**
 * AppointEntryTransferable
 *
 * @author  Kazushi Minagawa
 */ 
public final class AppointEntryTransferable implements Transferable, ClipboardOwner {

    /** Data Flavor of this class */
    public static DataFlavor appointFlavor = new DataFlavor(open.dolphin.infomodel.AppointmentModel.class, "AppointEntry");
  
    public static final DataFlavor[] flavors = {AppointEntryTransferable.appointFlavor};
      
    private final AppointmentModel appoint;

    /** Creates new StampTransferable */
    public AppointEntryTransferable(AppointmentModel appoint) {
        this.appoint = appoint;
    }

    @Override
    public synchronized DataFlavor[] getTransferDataFlavors() {
	return flavors;
    }
     
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
	return appointFlavor.equals(flavor);
    }

    @Override
    public synchronized Object getTransferData(DataFlavor flavor)
	    throws UnsupportedFlavorException, IOException {

        if (flavor.equals(appointFlavor)) {
            return appoint;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public String toString() {
        return "AppointEntryTransferable";
    }
  
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}