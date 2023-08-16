import pickle
from control import ss
from filterpy.common import Q_discrete_white_noise
from filterpy.kalman import KalmanFilter
from oomodelling import Model
import sympy as sp
import numpy as np


class Model:
    def __init__(self) -> None:
        self.k_filter = self.construct_filter(3.0,0.00001,145.69782402,0.79154106,
                                227.76228512,1.92343277,21,21)
        self.actual_temperature = 21.0
        self.real_temperature = 21.0
        self.in_heater = 0.0
        self.initial_heat_temperature = 21.0
        self.initial_box_temperature = 21.0
        self.real_a = 0.0
        self.real_b = 0.0
        self.integer_a = 0
        self.integer_b = 0
        self.boolean_a = False
        self.boolean_b = False
        self.string_a = ""
        self.string_b = ""

        self.reference_to_attribute = {
            0: "real_a",
            1: "real_b",
            2: "real_c",
            3: "integer_a",
            4: "integer_b",
            5: "integer_c",
            6: "boolean_a",
            7: "boolean_b",
            8: "boolean_c",
            9: "string_a",
            10: "string_b",
            11: "string_c",
            12: "real_temperature",
            13: "in_heater",
            14: "initial_heat_temperature",
            15: "initial_box_temperature",
            16: "actual_temperature",
        }

        self._update_outputs()

    def fmi2DoStep(self, current_time, step_size, no_step_prior):
        #self.k_filter = self.construct_filter(step_size,0.00001,145.69782402,0.79154106,
        #                        227.76228512,1.92343277,self.actual_temperature,self.actual_temperature)
        self.k_filter.predict(u=np.array([
            [self.in_heater],
            [self.initial_heat_temperature]
        ]))
        self.k_filter.update(np.array([[self.initial_box_temperature]]))
        self.real_temperature = self.k_filter.x[1,0]
        self._update_outputs()
        return Fmi2Status.ok

    def fmi2EnterInitializationMode(self):
        return Fmi2Status.ok

    def fmi2ExitInitializationMode(self):
        self._update_outputs()
        return Fmi2Status.ok

    def fmi2SetupExperiment(self, start_time, stop_time, tolerance):
        return Fmi2Status.ok

    def fmi2SetReal(self, references, values):
        return self._set_value(references, values)

    def fmi2SetInteger(self, references, values):
        return self._set_value(references, values)

    def fmi2SetBoolean(self, references, values):
        return self._set_value(references, values)

    def fmi2SetString(self, references, values):
        return self._set_value(references, values)

    def fmi2GetReal(self, references):
        return self._get_value(references)

    def fmi2GetInteger(self, references):
        return self._get_value(references)

    def fmi2GetBoolean(self, references):
        return self._get_value(references)

    def fmi2GetString(self, references):
        return self._get_value(references)

    def fmi2Reset(self):
        return Fmi2Status.ok

    def fmi2Terminate(self):
        return Fmi2Status.ok

    def fmi2ExtSerialize(self):

        bytes = pickle.dumps(
            (
                self.real_a,
                self.real_b,
                self.integer_a,
                self.integer_b,
                self.boolean_a,
                self.boolean_b,
                self.string_a,
                self.string_b,
            )
        )
        return Fmi2Status.ok, bytes

    def fmi2ExtDeserialize(self, bytes) -> int:
        (
            real_a,
            real_b,
            integer_a,
            integer_b,
            boolean_a,
            boolean_b,
            string_a,
            string_b,
        ) = pickle.loads(bytes)
        self.real_a = real_a
        self.real_b = real_b
        self.integer_a = integer_a
        self.integer_b = integer_b
        self.boolean_a = boolean_a
        self.boolean_b = boolean_b
        self.string_a = string_a
        self.string_b = string_b
        self._update_outputs()

        return Fmi2Status.ok

    def _set_value(self, references, values):

        for r, v in zip(references, values):
            setattr(self, self.reference_to_attribute[r], v)

        return Fmi2Status.ok

    def _get_value(self, references):

        values = []

        for r in references:
            values.append(getattr(self, self.reference_to_attribute[r]))

        return Fmi2Status.ok, values

    def _update_outputs(self):
        self.real_c = self.real_a + self.real_b
        self.integer_c = self.integer_a + self.integer_b
        self.boolean_c = self.boolean_a or self.boolean_b
        self.string_c = self.string_a + self.string_b

    def construct_filter(self,step_size, std_dev,
                        C_air_num,
                        G_box_num,
                        C_heater_num,
                        G_heater_num,
                        initial_heat_temperature,
                        initial_box_temperature):
        #Constants
        HEATER_VOLTAGE = 12.0
        HEATER_CURRENT = 10.45
        # Parameters
        C_air = sp.symbols("C_air")  # Specific heat capacity
        G_box = sp.symbols("G_box")  # Specific heat capacity
        C_heater = sp.symbols("C_heater")  # Specific heat capacity
        G_heater = sp.symbols("G_heater")  # Specific heat capacity

        # Constants
        V_heater = sp.symbols("V_heater")
        i_heater = sp.symbols("i_heater")

        # Inputs
        in_room_temperature = sp.symbols("T_room")
        on_heater = sp.symbols("on_heater")

        # States
        T = sp.symbols("T")
        T_heater = sp.symbols("T_h")

        power_in = on_heater * V_heater * i_heater

        power_transfer_heat = G_heater * (T_heater - T)

        total_power_heater = power_in - power_transfer_heat

        power_out_box = G_box * (T - in_room_temperature)

        total_power_box = power_transfer_heat - power_out_box

        der_T = (1.0 / C_air) * (total_power_box)
        der_T_heater = (1.0 / C_heater) * (total_power_heater)

        # Turn above into a CT linear system
        """
        States are:
        [[ T_heater ]
        [ T        ]]

        Inputs are:
        [ [ on_heater ],
        [ in_room_ptemperature ]]
        """
        A = sp.Matrix([
            [der_T_heater.diff(T_heater), der_T_heater.diff(T)],
            [der_T.diff(T_heater), der_T.diff(T)]
        ])

        B = sp.Matrix([
            [der_T_heater.diff(on_heater), der_T_heater.diff(in_room_temperature)],
            [der_T.diff(on_heater), der_T.diff(in_room_temperature)]
        ])

        # Observation matrix: only T can be measured
        C = sp.Matrix([[0.0, 1.0]])

        # Replace constants and get numerical matrices
        def replace_constants(m):
            return np.array(m.subs({
                V_heater: HEATER_VOLTAGE,
                i_heater: HEATER_CURRENT,
                C_air: C_air_num,
                G_box: G_box_num,
                C_heater: C_heater_num,
                G_heater: G_heater_num
            })).astype(np.float64)

        A_num, B_num, C_num = map(replace_constants, [A, B, C])

        ct_system = ss(A_num, B_num, C_num, np.array([[0.0, 0.0]]))
        dt_system = ct_system.sample(step_size, method="backward_diff")

        f = KalmanFilter(dim_x=2, dim_z=1, dim_u=2)
        f.x = np.array([[initial_heat_temperature],  # T_heater at t=0
                        [initial_box_temperature]])  # T at t=0
        f.F = dt_system.A
        f.B = dt_system.B
        f.H = dt_system.C
        f.P = np.array([[100., 0.],
                        [0., 100.]])
        f.R = np.array([[std_dev]])
        f.Q = Q_discrete_white_noise(dim=2, dt=step_size, var=std_dev ** 2)

        return f

    def reinitialize_filter(self,step_size,initial_heat_temperature, initial_box_temperature):
        self.k_filter = self.construct_filter(step_size,0.00001,145.69782402,0.79154106,
                                227.76228512,1.92343277,initial_heat_temperature,initial_box_temperature)

class Fmi2Status:
    """Represents the status of the FMU or the results of function calls.

    Values:
        * ok: all well
        * warning: an issue has arisen, but the computation can continue.
        * discard: an operation has resulted in invalid output, which must be discarded
        * error: an error has ocurred for this specific FMU instance.
        * fatal: an fatal error has ocurred which has corrupted ALL FMU instances.
        * pending: indicates that the FMu is doing work asynchronously, which can be retrived later.

    Notes:
        FMI section 2.1.3

    """

    ok = 0
    warning = 1
    discard = 2
    error = 3
    fatal = 4
    pending = 5


if __name__ == "__main__":
    m = Model()

    assert m.real_a == 0.0
    assert m.real_b == 0.0
    assert m.real_c == 0.0
    assert m.integer_a == 0
    assert m.integer_b == 0
    assert m.integer_c == 0
    assert m.boolean_a == False
    assert m.boolean_b == False
    assert m.boolean_c == False
    assert m.string_a == ""
    assert m.string_b == ""
    assert m.string_c == ""

    m.real_a = 1.0
    m.real_b = 2.0
    m.integer_a = 1
    m.integer_b = 2
    m.boolean_a = True
    m.boolean_b = False
    m.string_a = "Hello "
    m.string_b = "World!"

    assert m.fmi2DoStep(0.0, 1.0, False) == Fmi2Status.ok

    assert m.real_c == 3.0
    assert m.integer_c == 3
    assert m.boolean_c == True
    assert m.string_c == "Hello World!"
