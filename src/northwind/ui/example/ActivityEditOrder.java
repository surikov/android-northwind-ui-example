package northwind.ui.example;

import reactive.ui.*;
import tee.binding.*;
import tee.binding.it.*;
import tee.binding.task.*;
import android.os.*;
import android.app.*;
import android.content.*;
import android.database.sqlite.SQLiteStatement;
import android.graphics.*;

public class ActivityEditOrder extends Activity {
	Note id = new Note();
	Layoutless layoutless;
	Numeric freight = new Numeric();
	Note orderDate = new Note();
	Note customerID = new Note();
	Note customerName = new Note();
	Numeric employeeID = new Numeric();
	Note employeeName = new Note();
	Numeric requiredDate = new Numeric();
	Numeric shippedDate = new Numeric();
	Numeric viaID = new Numeric();
	Note shipName = new Note();
	Note shipAddress = new Note();
	Note shipCity = new Note();
	Note shipRegion = new Note();
	Note shipPostalCode = new Note();
	Note shipCountry = new Note();
	DataGrid details;
	ColumnText columnSupplier = new ColumnText();
	ColumnDescription columnProduct = new ColumnDescription();
	ColumnDescription columnPrice = new ColumnDescription();
	ColumnDescription columnQuantity = new ColumnDescription();
	final static int REQUEST_CUSTOMER = 1;
	final static int REQUEST_EMPLOYEE = 2;
	final static int REQUEST_ITEM = 3;
	Task save = new Task() {
		@Override
		public void doTask() {
			if (id.value().trim().length() > 0) {
				if (update()) {
					finish();
				}
			}
			else {
				if (insert()) {
					finish();
				}
			}
		}
	};
	Task delete = new Task() {
		@Override
		public void doTask() {
			if (id.value().trim().length() > 0) {
				Auxiliary.pick3Choice(ActivityEditOrder.this, "Delete order", "Are you sure?", "Delete", new Task() {
					@Override
					public void doTask() {
						Tools.db(ActivityEditOrder.this).execSQL("delete from [Order Details] where OrderID=" + id.value());
						Tools.db(ActivityEditOrder.this).execSQL("delete from Orders where OrderID=" + id.value());
						ActivityEditOrder.this.finish();
					}
				}, null, null, null, null);
			}
		}
	};
	Task promptNewItem = new Task() {
		@Override
		public void doTask() {
			promptItem("");
		}
	};
	Task promptEmployee = new Task() {
		@Override
		public void doTask() {
			Intent intent = new Intent();
			intent.setClass(ActivityEditOrder.this, ActivityPromptEmployee.class);
			ActivityEditOrder.this.startActivityForResult(intent, REQUEST_EMPLOYEE);
		}
	};
	Task promptCustomer = new Task() {
		@Override
		public void doTask() {
			Intent intent = new Intent();
			intent.setClass(ActivityEditOrder.this, ActivityPromptCustomer.class);
			ActivityEditOrder.this.startActivityForResult(intent, REQUEST_CUSTOMER);
		}
	};

	boolean insert() {
		if (customerID.value().length() < 1) {
			Auxiliary.inform("Choose customer", this);
			return false;
		}
		if (employeeID.value() < 1) {
			Auxiliary.inform("Choose emplyee", this);
			return false;
		}
		if (viaID.value() < 1) {
			Auxiliary.inform("Choose shipper", this);
			return false;
		}
		String newID = "" + (20000 + (Math.floor(100000 * Math.random())));
		String sh = Tools.sqliteFormat(shippedDate.value());
		if (shippedDate.value().intValue() == 0) {
			sh = "";
		}
		String sql = "insert into Orders ("//
				+ "\n	OrderID"//
				+ "\n	,CustomerID"//
				+ "\n	,EmployeeID"//
				+ "\n	,OrderDate"//
				+ "\n	,RequiredDate"//
				+ "\n	,ShippedDate"//
				+ "\n	,ShipVia"//
				+ "\n	,Freight"//
				+ "\n	,ShipName"//
				+ "\n	,ShipAddress"//
				+ "\n	,ShipCity"//
				+ "\n	,ShipRegion"//
				+ "\n	,ShipPostalCode"//
				+ "\n	,ShipCountry"//
				+ "\n	) values ("//
				+ "\n	" + newID//
				+ "\n	,'" + customerID.value() + "'"//
				+ "\n	," + employeeID.value()//
				+ "\n	,'" + orderDate.value() + "'"//
				+ "\n	,'" + Tools.sqliteFormat(requiredDate.value()) + "'"//
				+ "\n	,'" + sh + "'"//
				+ "\n	," + viaID.value()//
				+ "\n	," + freight.value()//
				+ "\n	,'" + shipName.value() + "'"//
				+ "\n	,'" + shipAddress.value() + "'"//
				+ "\n	,'" + shipCity.value() + "'"//
				+ "\n	,'" + shipRegion.value() + "'"//
				+ "\n	,'" + shipPostalCode.value() + "'"//
				+ "\n	,'" + shipCountry.value() + "'"//
				+ "\n	)"//
		;
		try {
			SQLiteStatement statement = Tools.db(this).compileStatement(sql);
			id.value("" + statement.executeInsert());
			return true;
		}
		catch (Throwable t) {
			Auxiliary.inform(t.getMessage(), this);
		}
		return false;
	}
	boolean update() {
		String sh = Tools.sqliteFormat(shippedDate.value());
		if (shippedDate.value().intValue() == 0) {
			sh = "";
		}
		String sql = "update Orders set"//
				+ "\n	CustomerID='" + customerID.value() + "'"//
				+ "\n	,EmployeeID=" + employeeID.value()//
				+ "\n	,RequiredDate='" + Tools.sqliteFormat(requiredDate.value()) + "'"//
				+ "\n	,ShippedDate='" + sh + "'"//
				+ "\n	,ShipVia=" + viaID.value()//
				+ "\n	,Freight=" + freight.value()//
				+ "\n	,ShipName='" + shipName.value() + "'"//
				+ "\n	,ShipAddress='" + shipAddress.value() + "'"//
				+ "\n	,ShipCity='" + shipCity.value() + "'"//
				+ "\n	,ShipRegion='" + shipRegion.value() + "'"//
				+ "\n	,ShipPostalCode='" + shipPostalCode.value() + "'"//
				+ "\n	,ShipCountry='" + shipCountry.value() + "'"//
				+ "\n	where OrderID=" + id.value()//
		;
		try {
			Tools.db(this).execSQL(sql);
			return true;
		}
		catch (Throwable t) {
			Auxiliary.inform(t.getMessage(), this);
		}
		return false;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			String s = bundle.getString("id");
			if (s != null) {
				this.setTitle("Northwind UI Example - Order " + s);
				id.value(s);
				scatterOrder(id.value());
			}
			else {
				this.setTitle("Northwind UI Example - New order");
				orderDate.value(Tools.formatDate(new java.util.Date()));
			}
		}
		else {
			this.setTitle("Northwind UI Example - New order");
			orderDate.value(Tools.formatDate(new java.util.Date()));
		}
		layoutless = new Layoutless(this);
		setContentView(layoutless);
		compose();
	}
	void scatterOrder(String id) {
		String sql = "select "//
				+ " 	Orders.OrderID,Orders.Freight,Orders.OrderDate "//
				+ " 		,Orders.CustomerID,Customers.CompanyName as CustomerName,Customers.City as CustomerCity "//
				+ " 		,Orders.EmployeeID,Employees.FirstName,Employees.LastName "//
				+ " 		,Orders.RequiredDate,Orders.ShippedDate "//
				+ " 		,Orders.ShipVia,Shippers.CompanyName as ShipperName "//
				+ " 		,Orders.ShipName,Orders.ShipAddress,Orders.ShipCity,Orders.ShipRegion,Orders.ShipPostalCode,Orders.ShipCountry "//
				+ " 	from orders "// 
				+ " 		join Customers on Customers.CustomerID=Orders.CustomerID "//
				+ " 		join Employees on Employees.EmployeeID=Orders.EmployeeID "//
				+ " 		join Shippers on Shippers.ShipperID=Orders.ShipVia "//
				+ " 	where orders.orderid=" + id;
		Bough data = Auxiliary.fromCursor(Tools.db(this).rawQuery(sql, null), true);
		Bough row = data.children.get(0);
		freight.value(Numeric.string2double(row.child("Freight").value.property.value()));
		orderDate.value(Tools.formatDate(Auxiliary.date(row.child("OrderDate").value.property.value())));
		customerID.value(row.child("CustomerID").value.property.value());
		customerName.value(row.child("CustomerName").value.property.value() + ", " + row.child("CustomerCity").value.property.value());
		employeeID.value(Numeric.string2double(row.child("EmployeeID").value.property.value()));
		employeeName.value(row.child("FirstName").value.property.value() + " " + row.child("LastName").value.property.value());
		requiredDate.value(Numeric.string2double(row.child("RequiredDate").value.property.value()));
		shippedDate.value(Numeric.string2double(row.child("ShippedDate").value.property.value()));
		viaID.value(Numeric.string2double(row.child("ShipVia").value.property.value()));
		shipName.value(row.child("ShipName").value.property.value());
		shipAddress.value(row.child("ShipAddress").value.property.value());
		shipCity.value(row.child("ShipCity").value.property.value());
		shipRegion.value(row.child("ShipRegion").value.property.value());
		shipPostalCode.value(row.child("ShipPostalCode").value.property.value());
		shipCountry.value(row.child("ShipCountry").value.property.value());
		requeryItems();
	}
	void compose() {
		Numeric itemsSplit = new Numeric().value(0.8 * Auxiliary.screenWidth(this));
		details = new DataGrid(this);
		layoutless//
		.innerHeight.is(15 * 0.8 * Auxiliary.tapSize)//
		.innerWidth.is(9 * Auxiliary.tapSize)//
		;
		layoutless.child(new Decor(this)//
				.background.is(Auxiliary.colorBackground)//
						.sketch(new SketchPlate().tint(new TintBitmapTile()//
								.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflakes3))//
								)//
						.width.is(layoutless.width().property)//
						.height.is(layoutless.height().property)//
						)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.child(new Decor(this)//
				.labelText.is("Order")//
				.labelSize.is(0.2 * Auxiliary.screenWidth(this))//
				.labelColor.is(0x22006699)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.child(new Decor(this)//
				.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflakes4)).top().is(layoutless.height().property.minus(250)).width().is(400)//
						.height().is(250)//
				);
		layoutless.field(this, 0, "ID", new Decor(this).labelText.is(id).labelAlignLeftCenter().labelStyleMediumNormal());
		layoutless.field(this, 1, "Order freight", new RedactNumber(this).number.is(freight));
		layoutless.field(this, 2, "Order date", new Decor(this).labelText.is(orderDate).labelAlignLeftCenter().labelStyleMediumNormal(), 3 * Auxiliary.tapSize);
		layoutless.field(this, 3, "Customer", new KnobText(this).text.is(customerName).afterTap.is(promptCustomer), 7 * Auxiliary.tapSize);
		layoutless.field(this, 4, "Employee", new KnobText(this).text.is(employeeName).afterTap.is(promptEmployee));
		layoutless.field(this, 5, "Required date", new RedactDate(this).date.is(requiredDate).format.is("MM/dd/yyyy"), 3 * Auxiliary.tapSize);
		layoutless.field(this, 6, "Shipped date", new RedactDate(this).date.is(shippedDate).format.is("MM/dd/yyyy"), 3 * Auxiliary.tapSize);
		layoutless.field(this, 7, "Ship via", new RedactSingleChoice(this).selection.is(viaID.minus(1)).item("Speedy Express").item("United Package").item("Federal Shipping"));
		layoutless.field(this, 8, "Ship name", new RedactText(this).text.is(shipName));
		layoutless.field(this, 9, "Ship address", new RedactText(this).text.is(shipAddress), 7 * Auxiliary.tapSize);
		layoutless.field(this, 10, "Ship city", new RedactText(this).text.is(shipCity));
		layoutless.field(this, 11, "Ship region", new RedactText(this).text.is(shipRegion));
		layoutless.field(this, 12, "Ship postal code", new RedactText(this).text.is(shipPostalCode), 3 * Auxiliary.tapSize);
		layoutless.field(this, 13, "Ship country", new RedactText(this).text.is(shipCountry));
		layoutless.child(new Knob(this)//
				.labelText.is("Clear")//
				.afterTap.is(new Task() {
					@Override
					public void doTask() {
						shippedDate.value(0);
					}
				})//
						.left().is(layoutless.shiftX.property.plus(layoutless.width().property.multiply(0.3).plus((0.1 + 3) * Auxiliary.tapSize)))//
						.top().is(layoutless.shiftY.property.plus(0.2 * Auxiliary.tapSize).plus(0.8 * 6 * Auxiliary.tapSize))//
						.width().is(1.5 * Auxiliary.tapSize)//
						.height().is(0.8 * Auxiliary.tapSize)//
				);
		layoutless.child(new Knob(this)//
				.labelText.is("Save")//
				.afterTap.is(save)//
						.top().is(layoutless.shiftY.property.plus(0.2 * Auxiliary.tapSize).plus(0.8 * 14 * Auxiliary.tapSize))//
						.left().is(layoutless.shiftX.property.plus(layoutless.width().property.multiply(0.3).plus(0.1 * Auxiliary.tapSize)))//
						.width().is(3 * Auxiliary.tapSize)//
						.height().is(0.8 * Auxiliary.tapSize)//
				);
		layoutless.child(new Knob(this)//
				.labelText.is("Delete")//
				.afterTap.is(delete)//
						.top().is(layoutless.shiftY.property.plus(0.2 * Auxiliary.tapSize).plus(0.8 * 14 * Auxiliary.tapSize))//
						.left().is(layoutless.shiftX.property.plus(layoutless.width().property.multiply(0.3).plus((3 + 0.1) * Auxiliary.tapSize)))//
						.width().is(3 * Auxiliary.tapSize)//
						.height().is(0.8 * Auxiliary.tapSize)//
				);
		layoutless.child(new Knob(this)//
				.labelText.is("New item")//
				.afterTap.is(promptNewItem)//
						.top().is(layoutless.shiftY.property.plus(0.2 * Auxiliary.tapSize).plus(0.8 * 14 * Auxiliary.tapSize))//
						.left().is(layoutless.shiftX.property.plus(layoutless.width().property.multiply(0.3).plus((3 + 3 + 0.1) * Auxiliary.tapSize)))//
						.width().is(3 * Auxiliary.tapSize)//
						.height().is(0.8 * Auxiliary.tapSize)//
				);
		layoutless.child(new Decor(this)//
				.background.is(Auxiliary.colorBackground)//
						.sketch(new SketchPlate().tint(new TintBitmapTile()//
								.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflakes3))//
								)//
						.width.is(layoutless.width().property)//
						.height.is(layoutless.height().property)//
						)//
						.left().is(itemsSplit)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.child(new Decor(this)//
				.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflake2))//
						.left().is(itemsSplit)//
						.width().is(322)//
						.height().is(406)//
				);
		layoutless.child(new SplitLeftRight(this)//
				.position.is(0)//
				.split.is(itemsSplit)//
						.rightSide(details//
								.columns(new Column[] { columnProduct.title.is("Product").width.is(4 * Auxiliary.tapSize) // 
										, columnSupplier.title.is("Supplier").width.is(5 * Auxiliary.tapSize) // 
										, columnPrice.title.is("Price").width.is(1.5 * Auxiliary.tapSize) //
										, columnQuantity.title.is("Quantity").width.is(2.5 * Auxiliary.tapSize) //
								})//
						)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.innerWidth.is(Auxiliary.screenWidth(this) * 0.3 + 9.1 * Auxiliary.tapSize);
		layoutless.innerHeight.is(0.8 * 15 * Auxiliary.tapSize + 2 * 0.2 * Auxiliary.tapSize);
	}
	void promptItem(String productID) {
		if (id.value().length() < 1) {
			if (!insert()) {
				return;
			}
		}
		Intent intent = new Intent();
		intent.setClass(this, ActivityEditItem.class);
		intent.putExtra("product", productID);
		intent.putExtra("date", orderDate.value());
		intent.putExtra("employee", employeeName.value());
		intent.putExtra("customer", customerName.value());
		intent.putExtra("order", id.value());
		this.startActivityForResult(intent, REQUEST_ITEM);
	}
	void onCustomerResult(String id) {
		String sql = "select CompanyName,City from Customers where CustomerID='" + id + "'";
		Bough data = Auxiliary.fromCursor(Tools.db(this).rawQuery(sql, null), true);
		Bough row = data.children.get(0);
		customerID.value(id);
		customerName.value(row.child("CompanyName").value.property.value() + ", " + row.child("City").value.property.value());
	}
	void onEmployeeResult(String id) {
		String sql = "select FirstName,LastName from Employees where EmployeeID=" + id;
		Bough data = Auxiliary.fromCursor(Tools.db(this).rawQuery(sql, null), true);
		Bough row = data.children.get(0);
		employeeID.value(Numeric.string2double(id));
		employeeName.value(row.child("FirstName").value.property.value() + " " + row.child("LastName").value.property.value());
	}
	void onItemResult() {
		details.clearColumns();
		requeryItems();
		details.refresh();
	}
	void requeryItems() {
		String sql = "select"//
				+ "		details.OrderID,details.ProductID,details.UnitPrice as OrderUnitPrice,details.Quantity,details.Discount"//
				+ "		,Products.ProductName,Products.QuantityPerUnit,Products.UnitPrice as ProductUnitPrice"//
				+ "		,Products.CategoryID,Categories.CategoryName"//
				+ "		,Products.SupplierID,Suppliers.CompanyName,Suppliers.City,Suppliers.Country"//
				+ "	from [Order Details] details"//
				+ "		join Products on Products.ProductID=details.ProductID"//
				+ "		join Suppliers on Suppliers.SupplierID=Products.SupplierID"//
				+ "		join Categories on Categories.CategoryID=Products.CategoryID"//
				+ "	where details.OrderID=" + id.value();
		Bough data = Auxiliary.fromCursor(Tools.db(this).rawQuery(sql, null), true);
		for (int i = 0; i < data.children.size(); i++) {
			Bough r = data.children.get(i);
			final String productID = r.child("ProductID").value.property.value();
			Task tapItem = new Task() {
				@Override
				public void doTask() {
					promptItem(productID);
				}
			};
			columnSupplier.cell(r.child("CompanyName").value.property.value() + ", " + r.child("Country").value.property.value() + ", " + r.child("City").value.property.value(), tapItem);
			columnProduct.cell(r.child("ProductName").value.property.value(), tapItem, r.child("CategoryName").value.property.value());
			columnPrice.cell(r.child("OrderUnitPrice").value.property.value(), tapItem, r.child("ProductUnitPrice").value.property.value() + "~" + r.child("Discount").value.property.value());
			columnQuantity.cell(r.child("Quantity").value.property.value(), tapItem, r.child("QuantityPerUnit").value.property.value());
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CUSTOMER) {
				onCustomerResult(intent.getStringExtra("customerID"));
			}
			if (requestCode == REQUEST_EMPLOYEE) {
				onEmployeeResult(intent.getStringExtra("employeeID"));
			}
			if (requestCode == REQUEST_ITEM) {
				onItemResult();
			}
		}
	}
}
