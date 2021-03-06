/*
 *
 *  Managed Data Structures
 *  Copyright © 2016 Hewlett Packard Enterprise Development Company LP.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  As an exception, the copyright holders of this Library grant you permission
 *  to (i) compile an Application with the Library, and (ii) distribute the 
 *  Application containing code generated by the Library and added to the 
 *  Application during this compilation process under terms of your choice, 
 *  provided you also meet the terms and conditions of the Application license.
 *
 */

#include "store.h"
#include <fstream>
#include <string>
#include <regex>

namespace {

  double parse_or(double def, const string &s) {
    if (s.empty()) {
      return def;
    }
    return stod(s);
  }
  unsigned parse_or_unsigned(unsigned def, const string &s) {
    if (s.empty()) {
      return def;
    }
    return stoul(s);
  }
}

string prod_file = "products.csv";

pair<mds_ptr<Store>,
     vector<double> >
init_store() {
  regex comma_re("\\s*,\\s*");
  
  StoreBuilder sb;
  ifstream in(prod_file);
  if (!in) {
    cout << "Product file '" << prod_file << "' not found" << endl;
    exit(-1);
  }
  string line;
  getline(in, line);            // Skip header line.

  vector<double> pops;

  using tokeniter = regex_token_iterator<string::iterator>;
  while (getline(in, line)) {
    vector<string> fields(tokeniter(line.begin(), line.end(), comma_re, -1),
                          tokeniter());
    // for (string s : fields) {
    //   cout << s << "|";
    // }
    // cout << endl;
    string dept_name = fields[0];
    string prod_name = fields[1];
    double pop = parse_or(1.0, fields[2]);
    unsigned shelf_life = parse_or_unsigned(0, fields[3]);
    unsigned initial = parse_or_unsigned(0, fields[4]);
    // unsigned restock_amt = parse_or_unsigned(0, fields[5]);
    // unsigned restock_freq = parse_or_unsigned(1, fields[6]);
    mds_ptr<Product> prod;
    if (shelf_life == 0) {
      prod = sb.add_non_perishable(dept_name, prod_name, initial);
    } else {
      prod = sb.add_perishable(dept_name, prod_name, shelf_life, initial);
    }
    // cout << prod << endl;
    pops.push_back(pop);
  }
  return make_pair(sb.build(), pops);
}

mds_ptr<Product>
choose_product(const mds_ptr<Store> &store, const vector<double> popularity)
{
  discrete_distribution<size_t> d(popularity.begin(), popularity.end());
  size_t which = d(tl_rand());
  return store->products[which];
}

mds_ptr<Basket>
make_basket(unsigned n, const mds_ptr<Store> &store, const vector<double> popularity)
{
  unordered_map<mds_ptr<Product>, mds_ptr<BasketItem>, hash<mds_ptr<mds_record> > >
    contents;
  cout << "Creating a basket with " << n << " items" << endl;
  for (unsigned i=0; i<n; i++) {
    mds_ptr<Product> p = choose_product(store, popularity);
    mds_ptr<BasketItem> &item = contents[p];
    if (item == nullptr) {
      item = new_record<BasketItem>(p, 1);
    } else {
      item->quantity++;
    }
  }
  vector<mds_ptr<BasketItem>> items;
  transform(contents.begin(), contents.end(), back_inserter(items),
            [](const auto &e) {
              return e.second;
            });
  mds_ptr<Basket> b = new_record<Basket>(items);
  cout << "Basket is " << b << endl;
  return b;
}

