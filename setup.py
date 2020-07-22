# Learn more about setup.py: https://github.com/kennethreitz/setup.py
import os
from setuptools import setup, find_packages
from codecs import open # Honestly, I am not sure if this is necessary.

about = {}

# here = os.path.abspath(os.path.dirname(__file__))
#
# with open(os.path.join(here, "parcelupdate", '__version__.py'), 'r', 'utf-8') as f:
#     exec(f.read(), about)

requires = [
    "requests",
    "psycopg2",
    "bs4",
    "click",
    "colorama",
]


setup(
    name="parcelupdate",
    version='0.0.1',
    description="short description",
    long_description="Looooooong description",
    author="Snapper Vibes",
    author_email="LearningWithSnapper@gmail.com",
    python_requires=">=3.8",
    # url=URL,
    # packages=find_packages(exclude=["tests", "*.tests", "*.tests.*", "tests.*", "parcelidlists"]),
    # If your package is a single module, use this instead of 'packages':
    py_modules=['parcelupdate',],

    install_requires=requires,
    include_package_data=True,
    license='MIT',
)