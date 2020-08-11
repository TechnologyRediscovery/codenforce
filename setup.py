# Learn more about setup.py: https://github.com/kennethreitz/setup.py
import os
from codecs import open
from setuptools import setup, find_packages

requires = [
    "requests",
    "psycopg2",
    "bs4",
    "click",
    "colorama",
]
extras_requires = {"dev": ["pytest", "pre-commit", "black"]}

HERE = os.path.abspath(os.path.dirname(__file__))
with open(os.path.join(HERE, "pyparcel", "__version__.py"), "r", "utf-8") as f:
    exec(f.read(), about := {})

description = "A command line interface for keeping Turtle Creek COG's CodeNForce's data up to date."

setup(
    name=about["__name__"],
    version=about["__version__"],
    description=description,
    long_description=description,
    author=about["__author__"],
    author_email=about["__email__"],
    python_requires=">=3.8",
    # url=URL,
    packages=find_packages(
        exclude=["tests", "*.tests", "*.tests.*", "tests.*", "parcelidlists"]
    ),
    # If your package is a single module, use this instead of 'packages':
    py_modules=["parcelupdate",],
    install_requires=requires,
    extras_require=extras_requires,
    include_package_data=True,
    license="GNU GENERAL PUBLIC LICENSE",
)
