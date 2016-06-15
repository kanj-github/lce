/**
 * Created by kanj on 15/6/16.
 */
public class GoogleLocation {
    private AssAddress address;

    public double[] getLocation() {
        return this.address.address.googleLatLng;
    }

    class AssAddress {
        InnerAssAddress address;
    }
    class InnerAssAddress {
        double googleLatLng[];
    }
}
