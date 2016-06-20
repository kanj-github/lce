/**
 * Created by kanj on 15/6/16.
 */
public class GoogleLocation {
    private AssAddress address;

    public double[] getLocation() {
        return this.address.address.googleLatLng;
    }

    public String getLine1() {
        if (address.address.addressLine1 != null) {
            return address.address.addressLine1;
        } else {
            return "";
        }
    }

    class AssAddress {
        InnerAssAddress address;
    }
    class InnerAssAddress {
        String addressLine1;
        double googleLatLng[];
    }
}
