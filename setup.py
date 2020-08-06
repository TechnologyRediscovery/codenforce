# Learn more about setup.py: https://github.com/kennethreitz/setup.py
import os
from setuptools import setup, find_packages

requires = [
    "requests",
    "psycopg2",
    "bs4",
    "click",
    "colorama",
]

about = {}

# setup values are set explicitly instead of reading from __version__ because packaging is difficult.
# Feel free to make changes so maintaining packaging is easier, but don't fret over it.
# This is the code that SHOULD have allowed for a more proper setup:
#       from codecs import open # Honestly, I am not sure if this is necessary.
#       HERE = os.path.abspath(os.path.dirname(__file__))
#       with open(os.path.join(HERE, "pyparcel", '__version__.py'), 'r', 'utf-8') as f:
#           exec(f.read(), about)

description = "A command line interface for keeping Turtle Creek COG's CodeNForce's data up to date."

setup(
    name="pyparcel",
    version='0.0.1',
    description=description,
    long_description=description,
    author="Snapper Vibes",
    author_email="LearningWithSnapper@gmail.com",
    python_requires=">=3.8",
    # url=URL,
    packages=find_packages(exclude=["tests", "*.tests", "*.tests.*", "tests.*", "parcelidlists"]),
    # If your package is a single module, use this instead of 'packages':
    py_modules=['parcelupdate',],
    install_requires=requires,
    extras_require={"dev":["pytest",]},
    include_package_data=True,
    license='GNU GENERAL PUBLIC LICENSE',
)
