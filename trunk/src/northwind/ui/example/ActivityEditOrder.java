package northwind.ui.example;

import reactive.ui.*;
import tee.binding.*;
import tee.binding.it.*;
import tee.binding.task.*;
import android.os.*;
import android.app.*;
import android.content.*;
import android.graphics.*;

public class ActivityEditOrder extends Activity {
	Note id = new Note();
	Layoutless layoutless;
	Note freight = new Note();
	Note date = new Note();
	Note customerID = new Note();
	Note customerName = new Note();
	Note employeeID = new Note();
	Note employeeName = new Note();
	Numeric requiredDate = new Numeric();
	Numeric shippedDate = new Numeric();
	Numeric viaID = new Numeric();
	//Note viaName = new Note();
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
	Task save = new Task() {
		@Override
		public void doTask() {
			System.out.println("save");
		}
	};
	Task delete = new Task() {
		@Override
		public void doTask() {
			System.out.println("delete");
		}
	};
	Task promptNewItem = new Task() {
		@Override
		public void doTask() {
			System.out.println("prompt new item");
		}
	};
	Task promptEmployee = new Task() {
		@Override
		public void doTask() {
			Intent intent = new Intent();
			intent.setClass(ActivityEditOrder.this, ActivityPromptEmployee.class);
			ActivityEditOrder.this.startActivityForResult(intent, 0);
		}
	};
	Task promptCustomer = new Task() {
		@Override
		public void doTask() {
			Intent intent = new Intent();
			intent.setClass(ActivityEditOrder.this, ActivityPromptCustomer.class);
			ActivityEditOrder.this.startActivityForResult(intent, 0);
		}
	};

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
			}
		}
		else {
			this.setTitle("Northwind UI Example - New order");
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
		freight.value(row.child("Freight").value.property.value());
		date.value(Tools.formatDate(Auxiliary.date(row.child("OrderDate").value.property.value())));
		customerID.value(row.child("CustomerID").value.property.value());
		customerName.value(row.child("CustomerName").value.property.value() + ", " + row.child("CustomerCity").value.property.value());
		employeeID.value(row.child("EmployeeID").value.property.value());
		employeeName.value(row.child("FirstName").value.property.value() + " " + row.child("LastName").value.property.value());
		requiredDate.value(Numeric.string2double(row.child("RequiredDate").value.property.value()));
		shippedDate.value(Numeric.string2double(row.child("ShippedDate").value.property.value()));
		viaID.value(Numeric.string2double(row.child("ShipVia").value.property.value()));
		//viaName.value(row.child("ShipperName").value.property.value());
		shipName.value(row.child("ShipName").value.property.value());
		shipAddress.value(row.child("ShipAddress").value.property.value());
		shipCity.value(row.child("ShipCity").value.property.value());
		shipRegion.value(row.child("ShipRegion").value.property.value());
		shipPostalCode.value(row.child("ShipPostalCode").value.property.value());
		shipCountry.value(row.child("ShipCountry").value.property.value());
		sql = "select"//
				+ "		details.OrderID,details.ProductID,details.UnitPrice as OrderUnitPrice,details.Quantity,details.Discount"//
				+ "		,Products.ProductName,Products.QuantityPerUnit,Products.UnitPrice as ProductUnitPrice"//
				+ "		,Products.CategoryID,Categories.CategoryName"//
				+ "		,Products.SupplierID,Suppliers.CompanyName,Suppliers.City,Suppliers.Country"//
				+ "	from [Order Details] details"//
				+ "		join Products on Products.ProductID=details.ProductID"//
				+ "		join Suppliers on Suppliers.SupplierID=Products.SupplierID"//
				+ "		join Categories on Categories.CategoryID=Products.CategoryID"//
				+ "	where details.OrderID=" + id;
		data = Auxiliary.fromCursor(Tools.db(this).rawQuery(sql, null), true);
		for (int i = 0; i < data.children.size(); i++) {
			Bough r = data.children.get(i);
			final String productID = r.child("ProductID").value.property.value();
			Task tapItem = new Task() {
				@Override
				public void doTask() {
					tapItem(productID);
				}
			};
			columnSupplier.cell(r.child("CompanyName").value.property.value() + ", " + r.child("Country").value.property.value() + ", " + r.child("City").value.property.value(), tapItem);
			columnProduct.cell(r.child("ProductName").value.property.value(), tapItem, r.child("CategoryName").value.property.value());
			columnPrice.cell(r.child("OrderUnitPrice").value.property.value(), tapItem, r.child("ProductUnitPrice").value.property.value() + "~" + r.child("Discount").value.property.value());
			columnQuantity.cell(r.child("Quantity").value.property.value(), tapItem, r.child("QuantityPerUnit").value.property.value());
		}
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
				.labelColor.is(0xccffffff)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.child(new Decor(this)//
				.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflakes4)).top().is(layoutless.height().property.minus(250)).width().is(400)//
						.height().is(250)//
				);
		//System.out.println("viaID "+viaID.value());
		layoutless.field(this, 0, "ID", new Decor(this).labelText.is(id).labelAlignLeftCenter().labelStyleMediumNormal());
		layoutless.field(this, 1, "Order freight", new Decor(this).labelText.is(freight).labelAlignLeftCenter().labelStyleMediumNormal());
		layoutless.field(this, 2, "Order date", new Decor(this).labelText.is(date).labelAlignLeftCenter().labelStyleMediumNormal(), 3 * Auxiliary.tapSize);
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
				.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflake2)).left().is(itemsSplit).width().is(322)//
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
		layoutless.innerWidth.is(Auxiliary.screenWidth(this) * 0.3 + 5.1 * Auxiliary.tapSize);
		layoutless.innerHeight.is(0.8 * 15 * Auxiliary.tapSize + 2 * 0.2 * Auxiliary.tapSize);
	}
	void tapItem(String productID) {
		Intent intent = new Intent();
		intent.setClass(this, ActivityEditItem.class);
		intent.putExtra("product", productID);
		intent.putExtra("order", id.value());
		this.startActivityForResult(intent, 0);
	}
}
