""" An under construction workspace enviroment. Code here is not production ready.
"""
from unittest import mock
from pyparcel import _events


class _Objects:
    def __init__(self):
        self.objects = []


Objects = _Objects()


def record_object():
    """ When a mocked method's return_value = record_object_class, the object that called the method will be appended to a global list.
    """
    Objects.objects.append("test")


thing = _events.Event()
thing.write_to_db = mock.MagicMock(return_value=True)
thing.write_to_db()

thing.write_to_db.assert_called_with(3, 4, 5, key="value")
